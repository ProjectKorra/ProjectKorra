package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class Torrent extends WaterAbility {

	private static final double CLEANUP_RANGE = 50;
	private static final Map<TempBlock, Pair<Player, Integer>> FROZEN_BLOCKS = new ConcurrentHashMap<>();

	private boolean sourceSelected;
	private boolean settingUp;
	private boolean forming;
	private boolean formed;
	private boolean launch;
	private boolean launching;
	private boolean freeze;
	private boolean revert;
	private int layer;
	@Attribute("FreezeRadius")
	private int maxLayer;
	private int maxHits;
	private int hits = 1;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long chargeTimeout;
	private long revertTime;
	private double startAngle;
	private double angle;
	@Attribute(Attribute.RADIUS) @DayNightFactor
	private double radius;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute("Successive" + Attribute.DAMAGE) @DayNightFactor
	private double successiveDamage;
	@Attribute("Deflect" + Attribute.DAMAGE) @DayNightFactor
	private double deflectDamage;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	private Block sourceBlock;
	private TempBlock source;
	private Location location;
	private ArrayList<TempBlock> blocks;
	private ArrayList<TempBlock> launchedBlocks;
	private ArrayList<Entity> hurtEntities;

	public Torrent(final Player player) {
		super(player);

		this.layer = 0;
		this.startAngle = 0;
		this.maxLayer = getConfig().getInt("Abilities.Water.Torrent.MaxLayer");
		this.knockback = getConfig().getDouble("Abilities.Water.Torrent.Knockback");
		this.angle = getConfig().getDouble("Abilities.Water.Torrent.Angle");
		this.radius = getConfig().getDouble("Abilities.Water.Torrent.Radius");
		this.knockup = getConfig().getDouble("Abilities.Water.Torrent.Knockup");
		this.interval = getConfig().getLong("Abilities.Water.Torrent.Interval");
		this.damage = getConfig().getDouble("Abilities.Water.Torrent.InitialDamage");
		this.successiveDamage = getConfig().getDouble("Abilities.Water.Torrent.SuccessiveDamage");
		this.maxHits = getConfig().getInt("Abilities.Water.Torrent.MaxHits");
		this.deflectDamage = getConfig().getDouble("Abilities.Water.Torrent.DeflectDamage");
		this.range = getConfig().getDouble("Abilities.Water.Torrent.Range");
		this.selectRange = getConfig().getDouble("Abilities.Water.Torrent.SelectRange");
		this.cooldown = getConfig().getLong("Abilities.Water.Torrent.Cooldown");
		this.chargeTimeout = getConfig().getLong("Abilities.Water.Torrent.ChargeTimeout");
		this.revert = getConfig().getBoolean("Abilities.Water.Torrent.Revert");
		this.revertTime = getConfig().getLong("Abilities.Water.Torrent.RevertTime");
		this.blocks = new ArrayList<>();
		this.launchedBlocks = new ArrayList<>();
		this.hurtEntities = new ArrayList<>();

		final Torrent oldTorrent = getAbility(player, Torrent.class);
		if (oldTorrent != null) {
			if (!oldTorrent.sourceSelected) {
				oldTorrent.use();
				this.bPlayer.addCooldown("Torrent", oldTorrent.cooldown);
				return;
			} else {
				oldTorrent.remove();
			}
		}

		if (this.bPlayer.isOnCooldown("Torrent")) {
			return;
		}


		this.time = System.currentTimeMillis();
		this.recalculateAttributes(); // Recalculate attributes to account for night factor
		this.sourceBlock = BlockSource.getWaterSourceBlock(player, this.selectRange, ClickType.LEFT_CLICK, true, true, this.bPlayer.canPlantbend());
		if (this.sourceBlock != null && !GeneralMethods.isRegionProtectedFromBuild(this, this.sourceBlock.getLocation())) {
			this.sourceSelected = true;
			this.start();
		}
	}

	private void freeze() {
		if (this.layer == 0) {
			return;
		} else if (!this.bPlayer.canBendIgnoreBindsCooldowns(getAbility("PhaseChange"))) {
			return;
		}
		final List<Block> ice = GeneralMethods.getBlocksAroundPoint(this.location, this.layer);
		final List<Entity> trapped = GeneralMethods.getEntitiesAroundPoint(this.location, this.layer);
		ICE_SETTING: for (final Block block : ice) {
			if (isTransparent(this.player, block) && block.getType() != Material.ICE) {
				for (final Entity entity : trapped) {
					if (entity instanceof Player) {
						if (Commands.invincible.contains(((Player) entity).getName())) {
							return;
						}
						if (!getConfig().getBoolean("Properties.Water.FreezePlayerHead") && GeneralMethods.playerHeadIsInBlock((Player) entity, block)) {
							continue ICE_SETTING;
						}
						if (!getConfig().getBoolean("Properties.Water.FreezePlayerFeet") && GeneralMethods.playerFeetIsInBlock((Player) entity, block)) {
							continue ICE_SETTING;
						}
					}
				}
				final TempBlock tblock = new TempBlock(block, Material.ICE.createBlockData(), this);
				tblock.setCanSuffocate(false);
				FROZEN_BLOCKS.put(tblock, Pair.of(this.player, this.getId()));
				if (this.revert) {
					tblock.setRevertTime(this.revertTime + (new Random().nextInt((500 + 500) + 1) - 500));
				}
				playIcebendingSound(block.getLocation());
			}
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.chargeTimeout > 0 && System.currentTimeMillis() > this.getStartTime() + this.chargeTimeout) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > this.time + this.interval) {
			this.time = System.currentTimeMillis();

			if (this.sourceSelected) {
				if (this.sourceBlock.getLocation().getWorld() != this.player.getWorld()) {
					this.remove();
					return;
				}

				if (this.sourceBlock.getLocation().distanceSquared(this.player.getLocation()) > this.selectRange * this.selectRange) {
					return;
				}

				if (this.player.isSneaking()) {
					this.sourceSelected = false;
					this.settingUp = true;

					if (TempBlock.isTempBlock(this.sourceBlock)) {
						final TempBlock origin = TempBlock.get(this.sourceBlock);

						if (FROZEN_BLOCKS.containsKey(origin)) {
							massThaw(origin);
						} else if (isBendableWaterTempBlock(origin)) {
							origin.revertBlock();
						}
					}

					if (isPlant(this.sourceBlock) || isSnow(this.sourceBlock)) {
						new PlantRegrowth(this.player, this.sourceBlock);
						this.sourceBlock.setType(Material.AIR);
					} else if (isCauldron(this.sourceBlock) || isTransformableBlock(this.sourceBlock)) {
						updateSourceBlock(this.sourceBlock);
					} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(this.sourceBlock)) {
						this.sourceBlock.setType(Material.AIR);
					}
					
					this.source = new TempBlock(this.sourceBlock, isCauldron(this.sourceBlock) ? this.sourceBlock.getBlockData() : Material.WATER.createBlockData());
					this.location = this.sourceBlock.getLocation();
				} else {
					playFocusWaterEffect(this.sourceBlock);
					return;
				}
			}

			if (this.settingUp) {
				if (!this.player.isSneaking()) {
					this.location = this.source.getLocation();
					this.remove();
					return;
				}

				final Location eyeLoc = this.player.getEyeLocation();
				final double startAngle = this.player.getEyeLocation().getDirection().angle(new Vector(1, 0, 0));
				final double dx = this.radius * Math.cos(startAngle);
				final double dy = 0;
				final double dz = this.radius * Math.sin(startAngle);
				final Location setup = eyeLoc.clone().add(dx, dy, dz);

				if (!this.location.getWorld().equals(this.player.getWorld())) {
					this.remove();
					return;
				} else if (this.location.distanceSquared(setup) > this.range * this.range) {
					this.remove();
					return;
				}

				if (this.location.getBlockY() > setup.getBlockY()) {
					final Vector direction = new Vector(0, -1, 0);
					this.location = this.location.clone().add(direction);
				} else if (this.location.getBlockY() < setup.getBlockY()) {
					final Vector direction = new Vector(0, 1, 0);
					this.location = this.location.clone().add(direction);
				} else {
					final Vector direction = GeneralMethods.getDirection(this.location, setup).normalize();
					this.location = this.location.clone().add(direction);
				}

				if (this.location.distanceSquared(setup) <= 1) {
					this.settingUp = false;
					this.source.revertBlock();
					this.source = null;
					this.forming = true;
				} else if (!this.location.getBlock().equals(this.source.getLocation().getBlock())) {
					this.source.revertBlock();
					this.source = null;
					final Block block = this.location.getBlock();
					if (!isTransparent(this.player, block)) {
						this.remove();
						return;
					}
					this.source = new TempBlock(this.location.getBlock(), isCauldron(this.location.getBlock()) ? this.location.getBlock().getBlockData() : Material.WATER.createBlockData());
				}
			}
			if (this.forming && !this.player.isSneaking()) {
				this.location = this.player.getEyeLocation().add(this.radius, 0, 0);
				this.remove();
				return;
			}

			if (this.forming || this.formed) {
				if ((new Random()).nextInt(4) == 0) {
					playWaterbendingSound(this.location);
				}
				for (double theta = this.startAngle; theta < this.angle + this.startAngle; theta += 20) {
					final Location loc = this.player.getEyeLocation();
					final double phi = Math.toRadians(theta);
					final double dx = Math.cos(phi) * this.radius;
					final double dy = 0;
					final double dz = Math.sin(phi) * this.radius;
					loc.add(dx, dy, dz);
					if (isWater(loc.getBlock()) && GeneralMethods.isAdjacentToThreeOrMoreSources(loc.getBlock())) {
						ParticleEffect.WATER_BUBBLE.display(loc.getBlock().getLocation().clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
					}
					loc.subtract(dx, dy, dz);
				}
				if (this.angle < 220) {
					this.angle += 20;
				} else {
					this.forming = false;
					this.formed = true;
				}

				this.formRing();
				if (this.blocks.isEmpty()) {
					this.remove();
					return;
				}

			}

			if (this.formed && !this.player.isSneaking() && !this.launch) {
				new TorrentWave(this.player, this.radius);
				this.remove();
				return;
			}

			if (this.launch && this.formed) {
				this.launching = true;
				this.launch = false;
				this.formed = false;
				if (!this.launch()) {
					this.returnWater(this.location);
					this.remove();
					return;
				}
			}

			if (this.launching) {
				if (!this.player.isSneaking()) {
					this.remove();
					return;
				}
				if (!this.launch()) {
					this.remove();
					return;
				}
			}
		}
	}

	private boolean launch() {
		if (this.launchedBlocks.isEmpty() && this.blocks.isEmpty()) {
			return false;
		}

		if (this.launchedBlocks.isEmpty()) {
			this.clearRing();
			final Location loc = this.player.getEyeLocation();
			final ArrayList<Block> doneBlocks = new ArrayList<Block>();
			for (double theta = this.startAngle; theta < this.angle + this.startAngle; theta += 20) {
				final double phi = Math.toRadians(theta);
				final double dx = Math.cos(phi) * this.radius;
				final double dy = 0;
				final double dz = Math.sin(phi) * this.radius;
				final Location blockloc = loc.clone().add(dx, dy, dz);

				if (Math.abs(theta - this.startAngle) < 10) {
					this.location = blockloc.clone();
				}

				final Block block = blockloc.getBlock();
				if (!doneBlocks.contains(block) && !GeneralMethods.isRegionProtectedFromBuild(this, blockloc)) {
					if (isTransparent(this.player, block)) {
						this.launchedBlocks.add(new TempBlock(block, Material.WATER));
						doneBlocks.add(block);
					} else if (!isTransparent(this.player, block)) {
						break;
					}
				}
			}
			if (this.launchedBlocks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}

		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range, this.hurtEntities);
		Location targetLoc = this.player.getTargetBlock(getTransparentMaterialSet(), (int) this.range).getLocation();
		if (target != null) {
			targetLoc = target.getLocation();
		}

		final ArrayList<TempBlock> newBlocks = new ArrayList<TempBlock>();
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.range + 5);
		final List<Entity> affectedEntities = new ArrayList<Entity>();
		final Block realBlock = this.launchedBlocks.get(0).getBlock();
		final Vector dir = GeneralMethods.getDirection(this.location, targetLoc).normalize();

		if (target != null) {
			targetLoc = this.location.clone().add(dir.clone().multiply(10));
		}
		if (this.layer == 0) {
			this.location = this.location.clone().add(dir);
		}

		final Block locBlock = this.location.getBlock();
		if (this.location.distanceSquared(this.player.getLocation()) > this.range * this.range || GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
			if (this.layer < this.maxLayer) {
				if (this.freeze || this.layer < 1) {
					this.layer++;
				}
			}
			if (this.launchedBlocks.size() == 1) {
				this.remove();
				return false;
			}
		} else if (!isTransparent(this.player, locBlock)) {
			if (this.layer < this.maxLayer) {
				if (this.layer == 0) {
					this.hurtEntities.clear();
				}
				if (this.freeze || this.layer < 1) {
					this.layer++;
				}
			}
			if (this.freeze) {
				this.freeze();
			} else if (this.launchedBlocks.size() == 1) {
				this.location = realBlock.getLocation();
				this.remove();
				return false;
			}
		} else {
			if (locBlock.equals(realBlock) && this.layer == 0) {
				return true;
			}
			if (locBlock.getLocation().distanceSquared(targetLoc) > 1) {
				if (isWater(locBlock)) {
					ParticleEffect.WATER_BUBBLE.display(locBlock.getLocation().clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
				}
				newBlocks.add(new TempBlock(locBlock, Material.WATER));
			} else {
				if (this.layer < this.maxLayer) {
					if (this.layer == 0) {
						this.hurtEntities.clear();
					}
					if (this.freeze || this.layer < 1) {
						this.layer++;
					}
				}
				if (this.freeze) {
					this.freeze();
				}
			}
		}

		for (int i = 0; i < this.launchedBlocks.size(); i++) {
			final TempBlock block = this.launchedBlocks.get(i);
			if (i == this.launchedBlocks.size() - 1) {
				block.revertBlock();
			} else {
				newBlocks.add(block);
				for (final Entity entity : entities) {
					if (entity.getWorld() != block.getBlock().getWorld()) {
						continue;
					}
					if (entity.getLocation().distanceSquared(block.getLocation()) <= 1.5 * 1.5 && !affectedEntities.contains(entity)) {
						if (i == 0) {
							this.affect(entity, dir);
						} else {
							this.affect(entity, GeneralMethods.getDirection(block.getLocation(), this.launchedBlocks.get(i - 1).getLocation()).normalize());
						}
						affectedEntities.add(entity);
					}
				}
			}
		}

		this.launchedBlocks.clear();
		this.launchedBlocks.addAll(newBlocks);

		if (this.launchedBlocks.isEmpty()) {
			return false;
		}
		return true;
	}

	private void formRing() {
		this.clearRing();
		this.startAngle += 30;

		final Location loc = this.player.getEyeLocation();
		final ArrayList<Block> doneBlocks = new ArrayList<Block>();
		final ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(loc, this.radius + 2);

		for (double theta = this.startAngle; theta < this.angle + this.startAngle; theta += 20) {
			final double phi = Math.toRadians(theta);
			final double dx = Math.cos(phi) * this.radius;
			final double dy = 0;
			final double dz = Math.sin(phi) * this.radius;
			final Location blockLoc = loc.clone().add(dx, dy, dz);
			final Block block = blockLoc.getBlock();
			if (!doneBlocks.contains(block)) {
				if (isTransparent(this.player, block)) {
					this.blocks.add(new TempBlock(block, Material.WATER));
					doneBlocks.add(block);
					for (final Entity entity : entities) {
						if (entity.getWorld() != blockLoc.getWorld()) {
							continue;
						}
						if (!affectedEntities.contains(entity) && entity.getLocation().distanceSquared(blockLoc) <= 1.5 * 1.5) {
							this.deflect(entity);
						}
					}
				}
			}
		}
	}

	private void clearRing() {
		for (final TempBlock block : this.blocks) {
			block.revertBlock();
		}
		this.blocks.clear();
	}

	@Override
	public void remove() {
		super.remove();
		this.clearRing();
		for (final TempBlock block : this.launchedBlocks) {
			block.revertBlock();
		}

		this.launchedBlocks.clear();
		if (this.source != null) {
			this.source.revertBlock();
		}

		if (this.location != null) {
			this.returnWater(this.location);
		}
	}

	private void returnWater(final Location location) {
		new WaterReturn(this.player, location.getBlock());
	}

	public static void create(final Player player) {
		if (hasAbility(player, Torrent.class)) {
			return;
		}

		if (WaterReturn.hasWaterBottle(player)) {
			final Location eyeLoc = player.getEyeLocation();
			final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				if (block.getType() != Material.WATER) {
					block.setType(Material.WATER);
					block.setBlockData(GeneralMethods.getWaterData(0));
				}
				final Torrent tor = new Torrent(player);

				if (tor.sourceSelected || tor.settingUp) {
					WaterReturn.emptyWaterBottle(player);
				}
				block.setType(Material.AIR);
			}
		}
	}

	private void use() {
		this.launch = true;
		if (this.launching) {
			this.freeze = true;
		}
	}

	private void deflect(final Entity entity) {
		if (entity.getEntityId() == this.player.getEntityId()) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
			return;
		}
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - this.player.getLocation().getX();
		z = entity.getLocation().getZ() - this.player.getLocation().getZ();

		mag = Math.sqrt(x * x + z * z);

		vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
		vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

		final Vector vec = new Vector(vx, 0, vz).normalize().multiply(this.knockback);
		final Vector velocity = entity.getVelocity();

		if (this.bPlayer.isAvatarState()) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}

		GeneralMethods.setVelocity(this, entity, velocity);
		entity.setFallDistance(0);
		if (entity instanceof LivingEntity) {
			DamageHandler.damageEntity(entity, this.deflectDamage, this);
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	private void affect(final Entity entity, final Vector direction) {
		if (entity.getEntityId() == this.player.getEntityId()) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName()))) {
			return;
		}
		if (direction.getY() > this.knockup) {
			direction.setY(this.knockup);
		}
		if (!this.freeze) {
			GeneralMethods.setVelocity(this, entity, direction.multiply(this.knockback));
		}
		if (entity instanceof LivingEntity && !this.hurtEntities.contains(entity)) {
			double damageDealt = this.damage;
			if (this.hits > 1 && this.hits <= this.maxHits) {
				damageDealt = this.successiveDamage;
			}
			if (this.hits == this.maxHits) {
				this.hits = this.maxHits + 1;
			} else {
				this.hits += 1;
			}
			DamageHandler.damageEntity(entity, damageDealt, this);
			AirAbility.breakBreathbendingHold(entity);
			this.hurtEntities.add(entity);
			((LivingEntity) entity).setNoDamageTicks(0);
		}
	}

	public static void progressAllCleanup() {
		for (final TempBlock block : FROZEN_BLOCKS.keySet()) {
			final Player player = FROZEN_BLOCKS.get(block).getLeft();
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				FROZEN_BLOCKS.remove(block);
				continue;
			} else if (block.getBlock().getType() != Material.ICE) {
				FROZEN_BLOCKS.remove(block);
				continue;
			} else if (!player.isOnline()) {
				thaw(block);
				continue;
			} else if (block.getBlock().getWorld() != player.getWorld()) {
				thaw(block);
				continue;
			} else if (block.getLocation().distanceSquared(player.getLocation()) > CLEANUP_RANGE * CLEANUP_RANGE || !bPlayer.canBendIgnoreBindsCooldowns(getAbility("Torrent"))) {
				thaw(block);
			}
		}
	}

	public static void thaw(final Block block) {
		if (TempBlock.isTempBlock(block)) {
			final TempBlock tblock = TempBlock.get(block);
			if (FROZEN_BLOCKS.containsKey(tblock)) {
				thaw(tblock);
			}
		}
	}

	public static void thaw(final TempBlock block) {
		block.revertBlock();
		FROZEN_BLOCKS.remove(block);
	}

	/**
	 * Thaws the entire mass of ice created by a torrent that the given block is
	 * part of
	 *
	 * @param origin part of the ice mass created by a torrent
	 */
	public static void massThaw(final TempBlock origin) {
		if (FROZEN_BLOCKS.containsKey(origin)) {
			final Player creator = FROZEN_BLOCKS.get(origin).getLeft();
			final int id = FROZEN_BLOCKS.get(origin).getRight();

			for (final TempBlock tb : FROZEN_BLOCKS.keySet()) {
				if (tb.equals(origin)) {
					continue;
				}

				final Player check = FROZEN_BLOCKS.get(tb).getLeft();
				final int id2 = FROZEN_BLOCKS.get(tb).getRight();
				if (creator.equals(check) && id == id2) {
					thaw(tb);
				}
			}

			thaw(origin);
		}
	}

	public static boolean canThaw(final Block block) {
		if (TempBlock.isTempBlock(block)) {
			final TempBlock tblock = TempBlock.get(block);
			return !FROZEN_BLOCKS.containsKey(tblock);
		}
		return true;
	}

	public static void removeCleanup() {
		for (final TempBlock block : FROZEN_BLOCKS.keySet()) {
			thaw(block);
		}
	}

	public static boolean wasBrokenFor(final Player player, final Block block) {
		final Torrent torrent = getAbility(player, Torrent.class);
		if (torrent != null) {
			if (torrent.sourceBlock == null) {
				return false;
			}
			if (torrent.sourceBlock.equals(block)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Torrent";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isCollidable() {
		return this.forming || this.formed || this.launch || this.launching;
	}

	@Override
	public boolean allowBreakPlants() {
		return false;
	}

	@Override
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final TempBlock tblock : this.blocks) {
			locations.add(tblock.getLocation());
		}
		for (final TempBlock tblock : this.launchedBlocks) {
			locations.add(tblock.getLocation());
		}
		return locations;
	}

	public boolean isSourceSelected() {
		return this.sourceSelected;
	}

	public void setSourceSelected(final boolean sourceSelected) {
		this.sourceSelected = sourceSelected;
	}

	public boolean isSettingUp() {
		return this.settingUp;
	}

	public void setSettingUp(final boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return this.forming;
	}

	public void setForming(final boolean forming) {
		this.forming = forming;
	}

	public boolean isFormed() {
		return this.formed;
	}

	public void setFormed(final boolean formed) {
		this.formed = formed;
	}

	public boolean isLaunch() {
		return this.launch;
	}

	public void setLaunch(final boolean launch) {
		this.launch = launch;
	}

	public boolean isLaunching() {
		return this.launching;
	}

	public void setLaunching(final boolean launching) {
		this.launching = launching;
	}

	public boolean isFreeze() {
		return this.freeze;
	}

	public void setFreeze(final boolean freeze) {
		this.freeze = freeze;
	}

	public int getLayer() {
		return this.layer;
	}

	public void setLayer(final int layer) {
		this.layer = layer;
	}

	public int getMaxLayer() {
		return this.maxLayer;
	}

	public void setMaxLayer(final int maxLayer) {
		this.maxLayer = maxLayer;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getStartAngle() {
		return this.startAngle;
	}

	public void setStartAngle(final double startAngle) {
		this.startAngle = startAngle;
	}

	public double getAngle() {
		return this.angle;
	}

	public void setAngle(final double angle) {
		this.angle = angle;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getPush() {
		return this.knockback;
	}

	public void setPush(final double push) {
		this.knockback = push;
	}

	public double getMaxUpwardForce() {
		return this.knockup;
	}

	public void setMaxUpwardForce(final double maxUpwardForce) {
		this.knockup = maxUpwardForce;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getDeflectDamage() {
		return this.deflectDamage;
	}

	public void setDeflectDamage(final double deflectDamage) {
		this.deflectDamage = deflectDamage;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getSource() {
		return this.source;
	}

	public void setSource(final TempBlock source) {
		this.source = source;
	}

	public ArrayList<TempBlock> getBlocks() {
		return this.blocks;
	}

	public void setBlocks(final ArrayList<TempBlock> blocks) {
		this.blocks = blocks;
	}

	public static double getCleanupRange() {
		return CLEANUP_RANGE;
	}

	public static Map<TempBlock, Player> getFrozenBlocks() {
		final Map<TempBlock, Player> blocks = new HashMap<>();
		for (final TempBlock tb : FROZEN_BLOCKS.keySet()) {
			blocks.put(tb, FROZEN_BLOCKS.get(tb).getLeft());
		}
		return blocks;
	}

	public ArrayList<TempBlock> getLaunchedBlocks() {
		return this.launchedBlocks;
	}

	public ArrayList<Entity> getHurtEntities() {
		return this.hurtEntities;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}
}
