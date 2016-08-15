package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class WaterSpoutWave extends WaterAbility {
	
	public static enum AbilityType {
		CLICK, SHIFT, RELEASE
	}

	public static enum AnimateState {
		RISE, TOWARD_PLAYER, CIRCLE, SHRINK
	}

	private static final ConcurrentHashMap<Block, TempBlock> FROZEN_BLOCKS = new ConcurrentHashMap<>();
	
	private double radius;
	private boolean charging;
	private boolean iceWave;
	private boolean iceOnly;
	private boolean moving;
	private boolean plant;
	private int progressCounter;
	private long time;
	private long cooldown;
	private double selectRange;
	private double speed;
	private double chargeTime;
	private double flightTime;
	private double waveRadius;
	private double thawRadius;
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
		
	public WaterSpoutWave(Player player, AbilityType type) {
		super(player);
		
		this.charging = false;
		this.iceWave = false;
		this.iceOnly = false;
		this.plant = getConfig().getBoolean("Abilities.Water.WaterSpout.Wave.AllowPlantSource");
		this.radius = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.Radius");
		this.waveRadius = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.WaveRadius");
		this.thawRadius = getConfig().getDouble("Abilities.Water.WaterCombo.IceWave.ThawRadius");
		this.animationSpeed = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.AnimationSpeed");
		this.selectRange = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.SelectRange");
		this.speed = getConfig().getDouble("Abilities.Water.WaterSpout.Wave.Speed");
		this.damage = getConfig().getDouble("Abilities.Water.WaterCombo.IceWave.Damage");
		this.chargeTime = getConfig().getLong("Abilities.Water.WaterSpout.Wave.ChargeTime");
		this.flightTime = getConfig().getLong("Abilities.Water.WaterSpout.Wave.FlightTime");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterSpout.Wave.Cooldown");
		this.affectedBlocks = new ConcurrentHashMap<>();
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();
		
		this.damage = getNightFactor(this.damage);
		
		if (!bPlayer.canBend(this)) {
			return;
		}

		this.time = System.currentTimeMillis();
		this.type = type;
		
		if (type == AbilityType.CLICK && CoreAbility.getAbility(player, WaterSpoutWave.class) != null) {
			WaterSpoutWave wave = CoreAbility.getAbility(player, WaterSpoutWave.class);
			if (wave.charging || wave.moving) {
				remove();
				return;
			}
		}
		
		start();
		
		if (type == AbilityType.CLICK) {
			 // Need to progress immediately for the WaterSpout check
			progress();
		}
	}

	@Override
	public void progress() {
		progressCounter++;
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (type != AbilityType.RELEASE) {
			if (!player.hasPermission("bending.ability.WaterSpout.Wave")) {
				remove();
				return;
			} else if (!bPlayer.getBoundAbilityName().equalsIgnoreCase(getName())) {
				remove();
				return;
			}
		}

		if (type == AbilityType.CLICK) {
			if (origin == null) {
				removeOldType(player, AbilityType.CLICK);
				Block block = getWaterSourceBlock(player, selectRange, plant);
				
				if (block == null) {
					remove();
					return;
				}
				
				Block blockAbove = block.getRelative(BlockFace.UP);
				if (blockAbove.getType() != Material.AIR && !isWaterbendable(blockAbove)) {
					remove();
					return;
				}
				
				origin = block.getLocation();
				if (!isWaterbendable(block) || GeneralMethods.isRegionProtectedFromBuild(this, origin)) {
					remove();
					return;
				} else if (iceOnly && !(isIcebendable(block) || isSnow(block))) {
					remove();
					return;
				}
			}
			
			if (player.getLocation().distanceSquared(origin) > selectRange * selectRange) {
				remove();
				return;
			} else if (player.isSneaking()) {
				setType(AbilityType.SHIFT);
				return;
			}
			playFocusWaterEffect(origin.getBlock());
		} else if (type == AbilityType.SHIFT) {
			if (direction == null) {
				direction = player.getEyeLocation().getDirection();
			}
			if (!charging) {
				if (!containsType(player, AbilityType.SHIFT)) {
					removeOldType(player, AbilityType.CLICK);
					remove();
					return;
				}
				
				charging = true;
				animation = AnimateState.RISE;
				location = origin.clone();
					
				if (isPlant(origin.getBlock()) || isSnow(origin.getBlock())) {
					new PlantRegrowth(player, origin.getBlock());
					origin.getBlock().setType(Material.AIR);
				}
			}

			removeOldType(player, AbilityType.CLICK);
			if (!player.isSneaking()) {
				if (System.currentTimeMillis() - time > chargeTime) {
					setType(AbilityType.RELEASE);
					setAnimation(AnimateState.SHRINK);
				} else {
					remove();
				}
				return;
			}

			if (animation == AnimateState.RISE && location != null) {
				revertBlocks();
				location.add(0, animationSpeed, 0);
				Block block = location.getBlock();
				
				if (!(isWaterbendable(block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
					remove();
					return;
				}
				createBlock(block, Material.STATIONARY_WATER);
				if (location.distanceSquared(origin) > 4) {
					animation = AnimateState.TOWARD_PLAYER;
				}
			} else if (animation == AnimateState.TOWARD_PLAYER) {
				revertBlocks();
				Location eyeLoc = player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
				eyeLoc.setY(player.getEyeLocation().getY());
				Vector vec = GeneralMethods.getDirection(location, eyeLoc);
				location.add(vec.normalize().multiply(animationSpeed));
				Block block = location.getBlock();
				
				if (!(isWaterbendable(block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
					remove();
					return;
				}

				createBlock(block, Material.STATIONARY_WATER);
				if (location.distanceSquared(eyeLoc) < 1.7) {
					animation = AnimateState.CIRCLE;
					Vector tempDir = player.getLocation().getDirection();
					tempDir.setY(0);
					direction = tempDir.normalize();
					revertBlocks();
				}
			} else if (animation == AnimateState.CIRCLE) {
				drawCircle(120, 5);
			}
		} else if (type == AbilityType.RELEASE) {
			if (animation == AnimateState.SHRINK) {
				radius -= 0.20;
				drawCircle(360, 15);
				
				if (radius < 1) {
					revertBlocks();
					time = System.currentTimeMillis();
					animation = null;
				}
			} else {
				moving = true;
				if ((System.currentTimeMillis() - time > flightTime && !bPlayer.isAvatarState()) || player.isSneaking()) {
					remove();
					return;
				}
				
				player.setFallDistance(0f);
				double currentSpeed = speed - (speed * (System.currentTimeMillis() - time) / flightTime);
				double nightSpeed = getNightFactor(currentSpeed * 0.9);
				currentSpeed = nightSpeed > currentSpeed ? nightSpeed : currentSpeed;
				if (bPlayer.isAvatarState()) {
					currentSpeed = getNightFactor(speed);
				}

				player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(currentSpeed));
				for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation().add(0, -1, 0), waveRadius)) {
					if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
						if (iceWave) {
							createBlockDelay(block, Material.ICE, (byte) 0, 2L);
						} else {
							createBlock(block, Material.STATIONARY_WATER, (byte) 0);
						}
					}
				}
				revertBlocksDelay(20L);

				if (iceWave && progressCounter % 3 == 0) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation().add(0, -1, 0), waveRadius * 1.5)) {
						if (entity != this.player && entity instanceof LivingEntity && !affectedEntities.contains(entity)) {
							affectedEntities.add(entity);
							final double augment = getNightFactor(player.getWorld());
							DamageHandler.damageEntity(entity, damage, this);
							final Player fplayer = this.player;
							final Entity fent = entity;
							
							new BukkitRunnable() {
								@Override
								public void run() {
									createIceSphere(fplayer, fent, augment * 2.5);
								}
							}.runTaskLater(ProjectKorra.plugin, 6);
						}
					}
					for (Block block : FROZEN_BLOCKS.keySet()) {
						TempBlock tBlock = FROZEN_BLOCKS.get(block);
						if (tBlock.getLocation().distance(player.getLocation()) >= thawRadius) {
							tBlock.revertBlock();
							FROZEN_BLOCKS.remove(block);
						}
					}
				}
			}
		}
	}

	public void drawCircle(double theta, double increment) {
		double rotateSpeed = 45;
		revertBlocks();
		direction = GeneralMethods.rotateXZ(direction, rotateSpeed);
		for (double i = 0; i < theta; i += increment) {
			Vector dir = GeneralMethods.rotateXZ(direction, i - theta / 2).normalize().multiply(radius);
			dir.setY(0);
			Block block = player.getEyeLocation().add(dir).getBlock();
			location = block.getLocation();
			if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				createBlock(block, Material.STATIONARY_WATER, (byte) 8);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (moving) {
			bPlayer.addCooldown(this);
		}
		revertBlocks();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
	}

	public void createBlockDelay(final Block block, final Material mat, final byte data, long delay) {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				createBlock(block, mat, data);
			}
		};
		br.runTaskLater(ProjectKorra.plugin, delay);
		tasks.add(br);
	}

	public void createBlock(Block block, Material mat) {
		createBlock(block, mat, (byte) 0);
	}

	public void createBlock(Block block, Material mat, byte data) {
		affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}

	public void revertBlocksDelay(long delay) {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			final Block block = keys.nextElement();
			final TempBlock tblock = affectedBlocks.get(block);
			affectedBlocks.remove(block);
			
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

	public void createIceSphere(Player player, Entity entity, double radius) {
		for (double x = -radius; x <= radius; x += 0.5) {
			for (double y = -radius; y <= radius; y += 0.5) {
				for (double z = -radius; z <= radius; z += 0.5) {
					Block block = entity.getLocation().getBlock().getLocation().add(x, y, z).getBlock();
					if (block.getLocation().distanceSquared(entity.getLocation().getBlock().getLocation()) > radius * radius) {
						continue;
					}

					if (block.getType() == Material.AIR || block.getType() == Material.ICE || isWaterbendable(block)) {
						if (!FROZEN_BLOCKS.containsKey(block)) {
							TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 1);
							FROZEN_BLOCKS.put(block, tblock);
						}
					}
				}
			}
		}
	}

	public static boolean containsType(Player player, AbilityType type) {
		for (WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type)) {
				return true;
			}
		}
		return false;
	}

	public void removeOldType(Player player, AbilityType type) {
		for (WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type) && !wave.equals(this)) {
				wave.remove();
			}
		}
	}

	public static ArrayList<WaterSpoutWave> getType(Player player, AbilityType type) {
		ArrayList<WaterSpoutWave> list = new ArrayList<WaterSpoutWave>();
		for (WaterSpoutWave wave : getAbilities(player, WaterSpoutWave.class)) {
			if (wave.type.equals(type)) {
				list.add(wave);
			}
		}
		return list;
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		ArrayList<WaterSpoutWave> waves = getType(player, AbilityType.CLICK);
		if (!waves.isEmpty()) {
			WaterSpoutWave wave = waves.get(0);
			if (wave.origin == null) {
				return false;
			} else if (wave.origin.getBlock().equals(block)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canThaw(Block block) {
		return FROZEN_BLOCKS.containsKey(block);
	}

	public static void thaw(Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			FROZEN_BLOCKS.get(block).revertBlock();
			FROZEN_BLOCKS.remove(block);
		}
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		} else {
			return origin;
		}
	}

	@Override
	public String getName() {
		return "WaterSpout";
	}
	
	@Override
	public Element getElement() 
	{
		return this.isIceWave() ? Element.ICE : Element.WATER;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return this.isIceWave() ? true : false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public boolean isCharging() {
		return charging;
	}

	public void setCharging(boolean charging) {
		this.charging = charging;
	}

	public boolean isIceWave() {
		return iceWave;
	}

	public void setIceWave(boolean iceWave) {
		this.iceWave = iceWave;
	}

	public boolean isIceOnly() {
		return iceOnly;
	}

	public void setIceOnly(boolean iceOnly) {
		this.iceOnly = iceOnly;
	}

	public boolean isEnabled() {
		return getConfig().getBoolean("Abilities.Water.WaterSpout.Wave.Enabled");
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public int getProgressCounter() {
		return progressCounter;
	}

	public void setProgressCounter(int progressCounter) {
		this.progressCounter = progressCounter;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getFlightTime() {
		return flightTime;
	}

	public void setFlightTime(double flightTime) {
		this.flightTime = flightTime;
	}

	public double getWaveRadius() {
		return waveRadius;
	}

	public void setWaveRadius(double waveRadius) {
		this.waveRadius = waveRadius;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public AbilityType getType() {
		return type;
	}

	public void setType(AbilityType type) {
		this.type = type;
	}

	public AnimateState getAnimation() {
		return animation;
	}

	public void setAnimation(AnimateState animation) {
		this.animation = animation;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public static ConcurrentHashMap<Block, TempBlock> getFrozenBlocks() {
		return FROZEN_BLOCKS;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public ConcurrentHashMap<Block, TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
		
}