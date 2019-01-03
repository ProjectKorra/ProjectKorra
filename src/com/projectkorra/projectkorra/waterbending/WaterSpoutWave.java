package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaterSpoutWave extends WaterAbility {

	public static enum AbilityType {
		CLICK, SHIFT, RELEASE
	}

	public static enum AnimateState {
		RISE, TOWARD_PLAYER, CIRCLE, SHRINK
	}

	private static final Map<Block, TempBlock> FROZEN_BLOCKS = new ConcurrentHashMap<>();

	@Attribute(Attribute.RADIUS)
	private double radius;
	private boolean charging;
	private boolean iceWave;
	private boolean iceOnly;
	private boolean moving;
	private boolean plant;
	private boolean collidable;
	private boolean revertIceSphere;
	private int progressCounter;
	private long time;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long revertSphereTime;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.CHARGE_DURATION)
	private double chargeTime;
	@Attribute("Flight" + Attribute.DURATION)
	private double flightDuration;
	@Attribute("Wave" + Attribute.RADIUS)
	private double waveRadius;
	@Attribute("Thaw" + Attribute.RADIUS)
	private double thawRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private double animationSpeed;
	private AbilityType type;
	private AnimateState animation;
	private Vector direction;
	private Location origin;
	private Location location;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;

	public WaterSpoutWave(final Player player, final AbilityType type) {
		super(player);

		this.charging = false;
		this.iceWave = false;
		this.iceOnly = false;
		this.collidable = false;
		this.plant = getConfig().getBoolean("Abilities.Water.WaterSpout.Wave.AllowPlantSource");
		this.radius = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.Radius");
		this.waveRadius = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.WaveRadius");
		this.thawRadius = getConfig().getDouble("Abilities.Water.IceWave.ThawRadius");
		this.animationSpeed = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.AnimationSpeed");
		this.selectRange = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.SelectRange");
		this.speed = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.Speed");
		this.damage = getConfig().getDouble("Abilities.Water.IceWave.Damage");
		this.chargeTime = getConfig().getLong("Abilities.Water.WaterSpout.Wave.ChargeTime");
		this.flightDuration = getConfig().getLong("Abilities.Water.WaterSpout.Wave.FlightDuration");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterSpout.Wave.Cooldown");
		this.revertSphereTime = getConfig().getLong("Abilities.Water.IceWave.RevertSphereTime");
		this.revertIceSphere = getConfig().getBoolean("Abilities.Water.IceWave.RevertSphere");
		this.affectedBlocks = new ConcurrentHashMap<>();
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = this.getNightFactor(this.damage);

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			this.chargeTime = 0;
			this.flightDuration = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.WaterWave.FlightDuration");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.IceWave.Damage");
			this.cooldown = 0;
		}

		this.time = System.currentTimeMillis();
		this.type = type;

		if (type == AbilityType.CLICK && CoreAbility.getAbility(player, WaterSpoutWave.class) != null) {
			final WaterSpoutWave wave = CoreAbility.getAbility(player, WaterSpoutWave.class);
			if (wave.charging || wave.moving) {
				this.remove();
				return;
			}
		}

		this.start();

		if (type == AbilityType.CLICK) {
			// Need to progress immediately for the WaterSpout check.
			this.progress();
		}
	}

	@Override
	public void progress() {
		this.progressCounter++;
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (CoreAbility.hasAbility(this.player, WaterSpout.class)) {
			final WaterSpout waterSpout = CoreAbility.getAbility(this.player, WaterSpout.class);
			waterSpout.remove();
		}

		if (this.type != AbilityType.RELEASE) {
			if (!this.player.hasPermission("bending.ability.WaterSpout.Wave")) {
				this.remove();
				return;
			} else if (!this.bPlayer.getBoundAbilityName().equals(this.getName())) {
				this.remove();
				return;
			}
		}

		if (this.type == AbilityType.CLICK) {
			if (this.origin == null) {
				this.removeOldType(this.player, AbilityType.CLICK);
				final Block block = getWaterSourceBlock(this.player, this.selectRange, this.plant);

				if (block == null) {
					this.remove();
					return;
				}

				final Block blockAbove = block.getRelative(BlockFace.UP);
				if (blockAbove.getType() != Material.AIR && !this.isWaterbendable(blockAbove)) {
					this.remove();
					return;
				}

				this.origin = block.getLocation();
				if (!this.isWaterbendable(block) || GeneralMethods.isRegionProtectedFromBuild(this, this.origin)) {
					this.remove();
					return;
				} else if (this.iceOnly && !(this.isIcebendable(block) || isSnow(block))) {
					this.remove();
					return;
				}
			}

			if (this.player.getLocation().distanceSquared(this.origin) > this.selectRange * this.selectRange) {
				this.remove();
				return;
			} else if (this.player.isSneaking()) {
				this.setType(AbilityType.SHIFT);
				return;
			}
			playFocusWaterEffect(this.origin.getBlock());
		} else if (this.type == AbilityType.SHIFT) {
			if (this.direction == null) {
				this.direction = this.player.getEyeLocation().getDirection();
			}
			if (!this.charging) {
				if (!containsType(this.player, AbilityType.SHIFT)) {
					this.removeOldType(this.player, AbilityType.CLICK);
					this.remove();
					return;
				}

				this.charging = true;
				this.animation = AnimateState.RISE;
				this.location = this.origin.clone();

				if (isPlant(this.origin.getBlock()) || isSnow(this.origin.getBlock())) {
					new PlantRegrowth(this.player, this.origin.getBlock());
					this.origin.getBlock().setType(Material.AIR);
				}
				
				if (TempBlock.isTempBlock(this.origin.getBlock())) {
					TempBlock tb = TempBlock.get(this.origin.getBlock());
					
					if (Torrent.getFrozenBlocks().containsKey(tb)) {
						Torrent.massThaw(tb);
					} else if (!isBendableWaterTempBlock(tb)) {
						this.remove();
						return;
					}
				}
			}

			this.removeOldType(this.player, AbilityType.CLICK);
			if (!this.player.isSneaking()) {
				if (System.currentTimeMillis() - this.time > this.chargeTime) {
					this.setType(AbilityType.RELEASE);
					this.setAnimation(AnimateState.SHRINK);
				} else {
					this.remove();
				}
				return;
			}

			if (this.animation == AnimateState.RISE && this.location != null) {
				this.revertBlocks();
				this.location.add(0, this.animationSpeed, 0);
				final Block block = this.location.getBlock();

				if (!(this.isWaterbendable(block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
					this.remove();
					return;
				}
				this.createBlock(block, Material.WATER);
				if (this.location.distanceSquared(this.origin) > 4) {
					this.animation = AnimateState.TOWARD_PLAYER;
				}
			} else if (this.animation == AnimateState.TOWARD_PLAYER) {
				this.revertBlocks();
				final Location eyeLoc = this.player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
				eyeLoc.setY(this.player.getEyeLocation().getY());
				final Vector vec = GeneralMethods.getDirection(this.location, eyeLoc);
				this.location.add(vec.normalize().multiply(this.animationSpeed));
				final Block block = this.location.getBlock();

				if (!(this.isWaterbendable(block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
					this.remove();
					return;
				}

				this.createBlock(block, Material.WATER);
				if (this.location.distanceSquared(eyeLoc) < 1.7) {
					this.animation = AnimateState.CIRCLE;
					final Vector tempDir = this.player.getLocation().getDirection();
					tempDir.setY(0);
					this.direction = tempDir.normalize();
					this.revertBlocks();
				}
			} else if (this.animation == AnimateState.CIRCLE) {
				this.drawCircle(120, 5);
			}
		} else if (this.type == AbilityType.RELEASE) {
			if (this.animation == AnimateState.SHRINK) {
				this.radius -= 0.20;
				this.drawCircle(360, 15);

				if (this.radius < 1) {
					this.revertBlocks();
					this.time = System.currentTimeMillis();
					this.animation = null;
				}
			} else {
				this.moving = true;
				this.collidable = true;
				if ((System.currentTimeMillis() - this.time > this.flightDuration && !this.bPlayer.isAvatarState()) || this.player.isSneaking()) {
					this.remove();
					return;
				}

				this.player.setFallDistance(0f);
				double currentSpeed = this.speed - (this.speed * (System.currentTimeMillis() - this.time) / this.flightDuration);
				final double nightSpeed = this.getNightFactor(currentSpeed * 0.9);
				currentSpeed = nightSpeed > currentSpeed ? nightSpeed : currentSpeed;
				if (this.bPlayer.isAvatarState()) {
					currentSpeed = this.getNightFactor(this.speed);
				}

				this.player.setVelocity(this.player.getEyeLocation().getDirection().normalize().multiply(currentSpeed));
				for (final Block block : GeneralMethods.getBlocksAroundPoint(this.player.getLocation().add(0, -1, 0), this.waveRadius)) {
					if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
						if (this.iceWave) {
							this.createBlockDelay(block, Material.ICE, Material.ICE.createBlockData(), 2L);
						} else {
							this.createBlock(block, Material.WATER, GeneralMethods.getWaterData(0));
						}
					}
				}
				this.revertBlocksDelay(20L);

				if (this.iceWave && this.progressCounter % 3 == 0) {
					for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.player.getLocation().add(0, -1, 0), this.waveRadius * 1.5)) {
						if (entity != this.player && entity instanceof LivingEntity && !this.affectedEntities.contains(entity)) {
							this.affectedEntities.add(entity);
							final double augment = getNightFactor(this.player.getWorld());
							DamageHandler.damageEntity(entity, this.damage, CoreAbility.getAbility(this.player, IceWave.class));
							final Player fplayer = this.player;
							final Entity fent = entity;

							new BukkitRunnable() {
								@Override
								public void run() {
									WaterSpoutWave.this.createIceSphere(fplayer, fent, augment * 2.5);
								}
							}.runTaskLater(ProjectKorra.plugin, 6);
						}
					}
					for (final Block block : FROZEN_BLOCKS.keySet()) {
						final TempBlock tBlock = FROZEN_BLOCKS.get(block);
						if (tBlock.getBlock().getWorld().equals(this.player.getWorld()) && tBlock.getLocation().distance(this.player.getLocation()) >= this.thawRadius) {
							tBlock.revertBlock();
							FROZEN_BLOCKS.remove(block);
						}
					}
				}
			}
		}
	}

	public void drawCircle(final double theta, final double increment) {
		final double rotateSpeed = 45;
		this.revertBlocks();
		this.direction = GeneralMethods.rotateXZ(this.direction, rotateSpeed);
		for (double i = 0; i < theta; i += increment) {
			final Vector dir = GeneralMethods.rotateXZ(this.direction, i - theta / 2).normalize().multiply(this.radius);
			dir.setY(0);
			final Block block = this.player.getEyeLocation().add(dir).getBlock();
			this.location = block.getLocation();
			if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				this.createBlock(block, Material.WATER, GeneralMethods.getWaterData(0));
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.moving) {
			this.bPlayer.addCooldown(this);
		}
		this.revertBlocks();
		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
	}

	public void createBlockDelay(final Block block, final Material mat, final BlockData data, final long delay) {
		final BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				WaterSpoutWave.this.createBlock(block, mat, data);
			}
		};
		br.runTaskLater(ProjectKorra.plugin, delay);
		this.tasks.add(br);
	}

	public void createBlock(final Block block, final Material mat) {
		this.createBlock(block, mat, mat.createBlockData());
	}

	public void createBlock(final Block block, final Material mat, final BlockData data) {
		this.affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public void revertBlocks() {
		final Enumeration<Block> keys = this.affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			final Block block = keys.nextElement();
			this.affectedBlocks.get(block).revertBlock();
			this.affectedBlocks.remove(block);
		}
	}

	public void revertBlocksDelay(final long delay) {
		final Enumeration<Block> keys = this.affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			final Block block = keys.nextElement();
			final TempBlock tblock = this.affectedBlocks.get(block);
			this.affectedBlocks.remove(block);

			new BukkitRunnable() {
				@Override
				public void run() {
					if (!FROZEN_BLOCKS.containsKey(block)) {
						tblock.revertBlock();
					}
				}
			}.runTaskLater(ProjectKorra.plugin, delay);
		}
	}

	public void createIceSphere(final Player player, final Entity entity, final double radius) {
		for (double x = -radius; x <= radius; x += 0.5) {
			for (double y = -radius; y <= radius; y += 0.5) {
				for (double z = -radius; z <= radius; z += 0.5) {
					final Block block = entity.getLocation().getBlock().getLocation().add(x, y, z).getBlock();
					if (block.getLocation().distanceSquared(entity.getLocation().getBlock().getLocation()) > radius * radius) {
						continue;
					}
					if(GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())){
						continue;
					}

					if (block.getType() == Material.AIR || block.getType() == Material.ICE || this.isWaterbendable(block)) {
						if (!FROZEN_BLOCKS.containsKey(block)) {
							final TempBlock tblock = new TempBlock(block, Material.ICE);
							FROZEN_BLOCKS.put(block, tblock);
							if (this.revertIceSphere) {
								tblock.setRevertTime(this.revertSphereTime + (new Random().nextInt(1000) - 500));
							}
						}
					}
				}
			}
		}
	}

	public static boolean containsType(final Player player, final AbilityType type) {
		for (final WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type)) {
				return true;
			}
		}
		return false;
	}

	public void removeOldType(final Player player, final AbilityType type) {
		for (final WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type) && !wave.equals(this)) {
				wave.remove();
			}
		}
	}

	public static ArrayList<WaterSpoutWave> getType(final Player player, final AbilityType type) {
		final ArrayList<WaterSpoutWave> list = new ArrayList<WaterSpoutWave>();
		for (final WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type)) {
				list.add(wave);
			}
		}
		return list;
	}

	public static boolean wasBrokenFor(final Player player, final Block block) {
		final ArrayList<WaterSpoutWave> waves = getType(player, AbilityType.CLICK);
		if (!waves.isEmpty()) {
			final WaterSpoutWave wave = waves.get(0);
			if (wave.origin == null) {
				return false;
			} else if (wave.origin.getBlock().equals(block)) {
				return true;
			}
		}
		return false;
	}

	public static void progressAllCleanup() {
		for (final Block block : FROZEN_BLOCKS.keySet()) {
			final TempBlock tb = FROZEN_BLOCKS.get(block);
			if (block.getType() != Material.ICE) {
				FROZEN_BLOCKS.remove(block);
				continue;
			}
			if (tb == null || !TempBlock.isTempBlock(block)) {
				FROZEN_BLOCKS.remove(block);
				continue;
			}
		}
	}

	public static boolean canThaw(final Block block) {
		return FROZEN_BLOCKS.containsKey(block);
	}

	public static void thaw(final Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			FROZEN_BLOCKS.get(block).revertBlock();
			FROZEN_BLOCKS.remove(block);
		}
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		} else {
			return this.origin;
		}
	}

	@Override
	public String getName() {
		return "WaterSpout";
	}

	@Override
	public Element getElement() {
		return this.isIceWave() ? Element.ICE : Element.WATER;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return this.isIceWave() ? true : false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isCollidable() {
		return this.collidable;
	}

	@Override
	public double getCollisionRadius() {
		return this.getRadius();
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public boolean isCharging() {
		return this.charging;
	}

	public void setCharging(final boolean charging) {
		this.charging = charging;
	}

	public boolean isIceWave() {
		return this.iceWave;
	}

	public void setIceWave(final boolean iceWave) {
		this.iceWave = iceWave;
	}

	public boolean isIceOnly() {
		return this.iceOnly;
	}

	public void setIceOnly(final boolean iceOnly) {
		this.iceOnly = iceOnly;
	}

	@Override
	public boolean isEnabled() {
		return getConfig().getBoolean("Abilities.Water.WaterSpout.Wave.Enabled");
	}

	public boolean isMoving() {
		return this.moving;
	}

	public void setMoving(final boolean moving) {
		this.moving = moving;
	}

	public int getProgressCounter() {
		return this.progressCounter;
	}

	public void setProgressCounter(final int progressCounter) {
		this.progressCounter = progressCounter;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getFlightDuration() {
		return this.flightDuration;
	}

	public void setFlightDuration(final double flightDuration) {
		this.flightDuration = flightDuration;
	}

	public double getWaveRadius() {
		return this.waveRadius;
	}

	public void setWaveRadius(final double waveRadius) {
		this.waveRadius = waveRadius;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getAnimationSpeed() {
		return this.animationSpeed;
	}

	public void setAnimationSpeed(final double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public AbilityType getType() {
		return this.type;
	}

	public void setType(final AbilityType type) {
		this.type = type;
	}

	public AnimateState getAnimation() {
		return this.animation;
	}

	public void setAnimation(final AnimateState animation) {
		this.animation = animation;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public static Map<Block, TempBlock> getFrozenBlocks() {
		return FROZEN_BLOCKS;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public ConcurrentHashMap<Block, TempBlock> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public boolean allowBreakPlants() {
		return false;
	}
}
