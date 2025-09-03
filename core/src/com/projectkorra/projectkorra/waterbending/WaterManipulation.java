package com.projectkorra.projectkorra.waterbending;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class WaterManipulation extends WaterAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<>();

	private static final BlockData WATER_6 = GeneralMethods.getWaterData(6);
	private static final BlockData WATER_7 = GeneralMethods.getWaterData(7);
	private static final BlockData WATER = Material.WATER.createBlockData();

	private boolean progressing;
	private boolean falling;
	private boolean settingUp;
	private boolean displacing;
	private boolean prepared;
	private int dispelRange;
	private long time;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	private long interval;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute("Deflect" + Attribute.RANGE) @DayNightFactor
	private double deflectRange;
	private double collisionRadius;
	private Block sourceBlock;
	private Location location;
	private TempBlock trail, trail2, source;
	private Location firstDestination;
	private Location targetDestination;
	private Vector firstDirection;
	private Vector targetDirection;

	public WaterManipulation(final Player player) {
		super(player);

		this.setFields();
		this.recalculateAttributes(); // So the select range is updated at night or in AvatarState

		Block block = prepare(player, this.selectRange);

		if (block != null) {
			this.sourceBlock = block;
			this.focusBlock();
			this.prepared = true;
			this.start();
			this.time = System.currentTimeMillis();
		}
	}

	public WaterManipulation(final Player player, final Block source) {
		super(player);

		this.setFields();

		if (source != null) {
			this.sourceBlock = source;
			this.focusBlock();
			this.prepared = true;
			this.start();
			this.time = System.currentTimeMillis();
		}
	}

	private void setFields() {
		this.progressing = false;
		this.falling = false;
		this.settingUp = false;
		this.displacing = false;
		this.collisionRadius = getConfig().getDouble("Abilities.Water.WaterManipulation.CollisionRadius");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterManipulation.Cooldown");
		this.selectRange = getConfig().getDouble("Abilities.Water.WaterManipulation.SelectRange");
		this.range = getConfig().getDouble("Abilities.Water.WaterManipulation.Range");
		this.knockback = getConfig().getDouble("Abilities.Water.WaterManipulation.Knockback");
		this.damage = getConfig().getDouble("Abilities.Water.WaterManipulation.Damage");
		this.speed = getConfig().getDouble("Abilities.Water.WaterManipulation.Speed");
		this.deflectRange = getConfig().getDouble("Abilities.Water.WaterManipulation.DeflectRange");

		this.interval = (long) (1000. / this.speed);
	}

	private static void cancelPrevious(final Player player) {
		final Collection<WaterManipulation> manips = getAbilities(player, WaterManipulation.class);
		for (final WaterManipulation oldmanip : manips) {
			if (oldmanip != null && !oldmanip.progressing) {
				oldmanip.remove();
			}
		}
	}

	private void finalRemoveWater(final Block block) {
		if (this.trail != null) {
			this.trail.revertBlock();
			this.trail = null;
		}
		if (this.trail2 != null) {
			this.trail2.revertBlock();
			this.trail2 = null;
		}
		if (this.source != null) {
			this.source.revertBlock();
			this.source = null;
		}
		if (this.displacing) {
			this.removeWater(block);
			return;
		}
		if (AFFECTED_BLOCKS.containsKey(block)) {
			if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.AIR);
			}
			AFFECTED_BLOCKS.remove(block);
		}
	}

	private void focusBlock() {
		this.location = this.sourceBlock.getLocation();
	}

	private Location getToEyeLevel() {
		final Location loc = this.sourceBlock.getLocation().clone();
		final double dy = this.targetDestination.getY() - this.sourceBlock.getY();
		if (dy <= 2) {
			loc.setY(this.sourceBlock.getY() + 2);
		} else {
			loc.setY(this.targetDestination.getY() - 1);
		}
		return loc;
	}

	public void moveWater() {
		if (this.sourceBlock != null) {
			if (this.sourceBlock.getWorld().equals(this.player.getWorld())) {
				this.targetDestination = getTargetLocation(this.player, this.range);

				if (this.targetDestination.distanceSquared(this.location) <= 1) {
					this.progressing = false;
					this.targetDestination = null;
					this.remove();
					return;
				} else {
					this.progressing = true;
					this.settingUp = true;
					this.firstDestination = this.getToEyeLevel();
					this.firstDirection = GeneralMethods.getDirection(this.sourceBlock.getLocation(), this.firstDestination).normalize();
					this.targetDestination = GeneralMethods.getPointOnLine(this.firstDestination, this.targetDestination, this.range);
					this.targetDirection = GeneralMethods.getDirection(this.firstDestination, this.targetDestination).normalize();
					
					if (isPlant(this.sourceBlock) || isSnow(this.sourceBlock)) {
						new PlantRegrowth(this.player, this.sourceBlock);
						this.sourceBlock.setType(Material.AIR);
					} else if (isCauldron(this.sourceBlock) || isTransformableBlock(this.sourceBlock)) {
						updateSourceBlock(this.sourceBlock);
					} else if (!isIce(this.sourceBlock)) {
						addWater(this.sourceBlock);
					}
				}
			}
			this.bPlayer.addCooldown(this);
		}
	}

	private static Block prepare(final Player player, final double selectRange) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		final Block block = BlockSource.getWaterSourceBlock(player, selectRange, ClickType.SHIFT_DOWN, true, true, bPlayer.canPlantbend());
		cancelPrevious(player);

		return block;
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.time >= this.interval) {
			if (!this.progressing && !this.falling && !this.bPlayer.getBoundAbilityName().equalsIgnoreCase(this.getName())) {
				this.remove();
				return;
			}

			if (this.falling) {
				this.remove();
				new WaterReturn(this.player, this.sourceBlock);
				return;
			} else {
				if (!this.progressing) {
					if (!(isWater(this.sourceBlock.getType()) || isCauldron(this.sourceBlock) || isMud(this.sourceBlock) || isSponge(this.sourceBlock) || (isIce(this.sourceBlock) && this.bPlayer.canIcebend()) || (isSnow(this.sourceBlock) && this.bPlayer.canIcebend()) || (isPlant(this.sourceBlock) && this.bPlayer.canPlantbend()))) {
						this.remove();
						return;
					}
					ParticleEffect.SMOKE_NORMAL.display(this.sourceBlock.getLocation().clone().add(0.5, 0.5, 0.5), 4, 0, 0, 0);
					return;
				}

				if (this.sourceBlock.getLocation().distanceSquared(this.firstDestination) < 0.5 * 0.5) {
					this.settingUp = false;
				}

				Vector direction;
				if (this.settingUp) {
					direction = this.firstDirection;
				} else {
					direction = this.targetDirection;
				}

				Block block = this.location.getBlock();
				if (this.displacing) {
					final Block targetBlock = this.player.getTargetBlock((HashSet<Material>) null, this.dispelRange);
					direction = GeneralMethods.getDirection(this.location, targetBlock.getLocation()).normalize();
					if (!this.location.getBlock().equals(targetBlock)) {
						this.location = this.location.clone().add(direction);

						block = this.location.getBlock();
						if (block.getLocation().equals(this.sourceBlock.getLocation())) {
							this.location = this.location.clone().add(direction);
							block = this.location.getBlock();
						}
					}
				} else {
					if ((new Random()).nextInt(4) == 0) {
						playWaterbendingSound(this.location);
					}
					this.location = this.location.clone().add(direction);
					block = this.location.getBlock();
					if (block.getLocation().equals(this.sourceBlock.getLocation())) {
						this.location = this.location.clone().add(direction);
						block = this.location.getBlock();
					}
				}

				/*if (this.trail2 != null) {
					if (!TempBlock.isTempBlock(block) && (this.trail2.getBlock().equals(block))) {
						this.trail2.revertBlock();
						this.trail2 = null;
					}
				}

				if (this.trail != null) {
					if (!TempBlock.isTempBlock(block) && this.trail.getBlock().equals(block)) {
						this.trail.revertBlock();
						this.trail = null;
						if (this.trail2 != null) {
							this.trail2.revertBlock();
							this.trail2 = null;
						}
					}
				}*/

				if (isTransparent(this.player, block) && !block.isLiquid()) {
					GeneralMethods.breakBlock(block);
				} else if (block.getType() != Material.AIR && !isWater(block)) {
					this.remove();
					new WaterReturn(this.player, this.sourceBlock);
					return;
				}

				if (!this.displacing) {
					for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius)) {
						if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId()) {
							if (RegionProtection.isRegionProtected(this.player, entity.getLocation(), "WaterManipulation") || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
								continue;
							}
							final Location location = this.player.getEyeLocation();
							final Vector vector = location.getDirection();
							GeneralMethods.setVelocity(this, entity, vector.normalize().multiply(this.knockback));

							DamageHandler.damageEntity(entity, this.damage, this);
							AirAbility.breakBreathbendingHold(entity);
							this.progressing = false;
						}
					}
				}

				if (!this.progressing) {
					this.remove();
					new WaterReturn(this.player, this.sourceBlock);
					return;
				}

				addWater(block);
				this.reduceWater(this.sourceBlock);

				if (this.trail2 != null) {
					this.trail2.revertBlock();
					this.trail2 = null;
				}
				if (this.trail != null) {
					this.trail2 = new TempBlock(this.trail.getBlock(), WATER_6, this);
					this.trail.revertBlock();
				}
				this.trail = new TempBlock(this.sourceBlock, WATER_7, this);
				this.sourceBlock = block;

				if (this.location.distanceSquared(this.targetDestination) <= 1 || this.location.distanceSquared(this.firstDestination) > this.range * this.range) {
					this.falling = true;
					this.progressing = false;
				}
			}
		}
	}

	private void redirect(final Player player, final Location targetlocation) {
		if (this.progressing && !this.settingUp) {
			if (this.location.distanceSquared(player.getLocation()) <= this.range * this.range) {
				this.targetDirection = GeneralMethods.getDirection(this.location, targetlocation).normalize();
			}
			this.targetDestination = targetlocation;
			this.setPlayer(player);
		}
	}

	private void reduceWater(final Block block) {
		if (this.displacing) {
			this.removeWater(block);
			return;
		}
		if (AFFECTED_BLOCKS.containsKey(block)) {
			if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block) && !isTransformableBlock(block)) {
				block.setType(Material.AIR);
			}
			AFFECTED_BLOCKS.remove(block);
		}
	}

	private void removeWater(final Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block) && !isTransformableBlock(block)) {
					block.setType(Material.AIR);
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	private void addWater(final Block block) {
		if (!isWater(block)) {
			if (!AFFECTED_BLOCKS.containsKey(block)) {
				AFFECTED_BLOCKS.put(block, block);
			}
			if (PhaseChange.getFrozenBlocksAsBlock().contains(block)) {
				PhaseChange.thaw(block);
			}
			if (this.source != null) this.source.revertBlock();
			this.source = new TempBlock(block, WATER, this);
		} else {
			if (isWater(block) && !AFFECTED_BLOCKS.containsKey(block)) {
				ParticleEffect.WATER_BUBBLE.display(block.getLocation().clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
			}
		}

	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean annihilateBlasts(final Location location, final double radius, final Player player) {
		boolean broke = false;
		for (final WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (manip.location.getWorld().equals(location.getWorld()) && !player.equals(manip.player) && manip.progressing) {
				if (manip.location.distanceSquared(location) <= radius * radius) {
					manip.remove();
					broke = true;
				}
			}
		}
		return broke;
	}

	/** Blocks other water manips */
	public static void block(final Player player) {
		for (final WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (!manip.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!manip.progressing) {
				continue;
			} else if (manip.getPlayer().equals(player)) {
				continue;
			} else if (RegionProtection.isRegionProtected(manip, manip.location)) {
				continue;
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = manip.location;
			if (mloc.distanceSquared(location) <= manip.selectRange * manip.selectRange && GeneralMethods.getDistanceFromLine(vector, location, manip.location) < manip.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				manip.remove();
			}
		}
	}

	public static boolean canBubbleWater(final Block block) {
		return canPhysicsChange(block);
	}

	public static boolean canFlowFromTo(final Block from, final Block to) {
		if (AFFECTED_BLOCKS.containsKey(to) || AFFECTED_BLOCKS.containsKey(from)) {
			return false;
		} else if (WaterSpout.getAffectedBlocks().containsKey(to) || WaterSpout.getAffectedBlocks().containsKey(from)) {
			return false;
		} else if (SurgeWall.getAffectedBlocks().containsKey(to) || SurgeWall.getAffectedBlocks().containsKey(from)) {
			return false;
		} else if (SurgeWall.getWallBlocks().containsKey(to) || SurgeWall.getWallBlocks().containsKey(from)) {
			return false;
		} else if (SurgeWave.isBlockWave(to) || SurgeWave.isBlockWave(from)) {
			return false;
		} else if (isAdjacentToFrozenBlock(to) || isAdjacentToFrozenBlock(from)) {
			return false;
		}
		return true;
	}

	public static boolean canPhysicsChange(final Block block) {
		if (AFFECTED_BLOCKS.containsKey(block)) {
			return false;
		} else if (WaterSpout.getAffectedBlocks().containsKey(block)) {
			return false;
		} else if (SurgeWall.getAffectedBlocks().containsKey(block)) {
			return false;
		} else if (SurgeWall.getWallBlocks().containsKey(block)) {
			return false;
		} else if (SurgeWave.isBlockWave(block)) {
			return false;
		} else if (TempBlock.isTempBlock(block) && !WaterAbility.isBendableWaterTempBlock(block)) {
			return false;
		}
		return true;
	}

	private static Location getTargetLocation(final Player player, final double range) {
		Location location;
		final Entity target = GeneralMethods.getTargetedEntity(player, range);

		if (target == null) {
			location = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
		} else {
			location = target.getLocation();
		}
		return location;
	}

	public static void moveWater(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		if (bPlayer.isOnCooldown("WaterManipulation")) {
			redirectTargettedBlasts(player);
			return;
		}

		boolean handledPrepare = false;
		double range = 25;
		for (final WaterManipulation waterManip : getAbilities(player, WaterManipulation.class)) {
			range = waterManip.range;
			if (waterManip.prepared) {
				waterManip.prepared = false;
				handledPrepare = true;
				waterManip.moveWater();
			}
		}

		if (redirectTargettedBlasts(player)) {
			// Don't create a new WaterManipulation if one was redirected.
			return;
		}

		if (!handledPrepare && WaterReturn.hasWaterBottle(player)) {
			final Location eyeLoc = player.getEyeLocation();
			final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			if (!AFFECTED_BLOCKS.containsKey(block)) {
				AFFECTED_BLOCKS.put(block, block);
			}

			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				if (getTargetLocation(player, range).distanceSquared(block.getLocation()) > 1) {
					final TempBlock tb = new TempBlock(block, WATER);

					final WaterManipulation waterManip = new WaterManipulation(player, block);
					waterManip.moveWater();
					if (waterManip.progressing) {
						WaterReturn.emptyWaterBottle(player);
					}
					tb.revertBlock();
				}
			}
		}
	}

	private static boolean redirectTargettedBlasts(final Player player) {
		boolean redirected = false;

		for (final WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (!manip.progressing) {
				continue;
			} else if (!manip.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (RegionProtection.isRegionProtected(player, manip.location, "WaterManipulation")) {
				continue;
			}

			if (manip.player.equals(player)) {
				manip.redirect(player, getTargetLocation(player, manip.range));
				redirected = true;
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = manip.location;
			if (mloc.distanceSquared(location) <= manip.selectRange * manip.selectRange && GeneralMethods.getDistanceFromLine(vector, location, manip.location) < manip.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				manip.redirect(player, getTargetLocation(player, manip.range));
				redirected = true;
			}
		}

		return redirected;
	}

	@Override
	public void remove() {
		super.remove();
		this.finalRemoveWater(this.sourceBlock);
	}

	public static void removeAroundPoint(final Location location, final double radius) {
		for (final WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (manip.location.getWorld().equals(location.getWorld())) {
				if (manip.location.distanceSquared(location) <= radius * radius) {
					manip.remove();
				}
			}
		}
	}

	@Override
	public String getName() {
		return "WaterManipulation";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		} else if (this.sourceBlock != null) {
			return this.sourceBlock.getLocation();
		}
		return null;
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
		return this.progressing;
	}

	@Override
	public double getCollisionRadius() {
		return this.collisionRadius;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			new WaterReturn(this.player, this.sourceBlock);
		}
	}

	public boolean isProgressing() {
		return this.progressing;
	}

	public void setProgressing(final boolean progressing) {
		this.progressing = progressing;
	}

	public boolean isFalling() {
		return this.falling;
	}

	public void setFalling(final boolean falling) {
		this.falling = falling;
	}

	public boolean isSettingUp() {
		return this.settingUp;
	}

	public void setSettingUp(final boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isDisplacing() {
		return this.displacing;
	}

	public void setDisplacing(final boolean displacing) {
		this.displacing = displacing;
	}

	public boolean isPrepared() {
		return this.prepared;
	}

	public void setPrepared(final boolean prepared) {
		this.prepared = prepared;
	}

	public int getDispelRange() {
		return this.dispelRange;
	}

	public void setDispelRange(final int dispelRange) {
		this.dispelRange = dispelRange;
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

	public double getPushFactor() {
		return this.knockback;
	}

	public void setPushFactor(final double pushFactor) {
		this.knockback = pushFactor;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getDeflectRange() {
		return this.deflectRange;
	}

	public void setDeflectRange(final double deflectRange) {
		this.deflectRange = deflectRange;
	}

	@Override
	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getTrail() {
		return this.trail;
	}

	public void setTrail(final TempBlock trail) {
		this.trail = trail;
	}

	public TempBlock getTrail2() {
		return this.trail2;
	}

	public void setTrail2(final TempBlock trail2) {
		this.trail2 = trail2;
	}

	public Location getFirstDestination() {
		return this.firstDestination;
	}

	public void setFirstDestination(final Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getTargetDestination() {
		return this.targetDestination;
	}

	public void setTargetDestination(final Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Vector getFirstDirection() {
		return this.firstDirection;
	}

	public void setFirstDirection(final Vector firstDirection) {
		this.firstDirection = firstDirection;
	}

	public Vector getTargetDirection() {
		return this.targetDirection;
	}

	public void setTargetDirection(final Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

}
