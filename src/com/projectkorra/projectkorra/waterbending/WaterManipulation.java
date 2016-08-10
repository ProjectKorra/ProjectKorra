package com.projectkorra.projectkorra.waterbending;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class WaterManipulation extends WaterAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<>();

	private boolean progressing;
	private boolean falling;
	private boolean settingUp;
	private boolean displacing;
	private boolean prepared;
	private int dispelRange;
	private long time;
	private long cooldown;
	private long interval;
	private double selectRange, range;
	private double pushFactor;
	private double damage;
	private double speed;
	private double deflectRange;
	private double collisionRadius;
	private Block sourceBlock;
	private Location location;
	private TempBlock trail;
	private TempBlock trail2;
	private Location firstDestination;
	private Location targetDestination;
	private Vector firstDirection;
	private Vector targetDirection;
	private HashSet<Byte> waterTypes;

	public WaterManipulation(Player player) {
		super(player);

		this.progressing = false;
		this.falling = false;
		this.settingUp = false;
		this.displacing = false;
		this.collisionRadius = getConfig().getDouble("Abilities.Water.WaterManipulation.CollisionRadius");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterManipulation.Cooldown");
		this.selectRange = getConfig().getDouble("Abilities.Water.WaterManipulation.SelectRange");
		this.range = getConfig().getDouble("Abilities.Water.WaterManipulation.Range");
		this.pushFactor = getConfig().getDouble("Abilities.Water.WaterManipulation.Push");
		this.damage = getConfig().getDouble("Abilities.Water.WaterManipulation.Damage");
		this.speed = getConfig().getDouble("Abilities.Water.WaterManipulation.Speed");
		this.deflectRange = getConfig().getDouble("Abilities.Water.WaterManipulation.DeflectRange");
		this.waterTypes = new HashSet<Byte>();

		this.interval = (long) (1000. / speed);
		this.waterTypes.add((byte) 0);
		this.waterTypes.add((byte) 8);
		this.waterTypes.add((byte) 9);

		if (prepare()) {
			prepared = true;
			start();
			time = System.currentTimeMillis();
		}
	}

	private void cancelPrevious() {
		Collection<WaterManipulation> manips = getAbilities(player, WaterManipulation.class);
		for (WaterManipulation oldmanip : manips) {
			if (oldmanip != null && !oldmanip.progressing) {
				oldmanip.remove();
			}
		}
	}

	private void finalRemoveWater(Block block) {
		if (trail != null) {
			trail.revertBlock();
			trail = null;
		}
		if (trail2 != null) {
			trail2.revertBlock();
			trail = null;
		}
		if (displacing) {
			removeWater(block);
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
		location = sourceBlock.getLocation();
	}

	private Location getToEyeLevel() {
		Location loc = sourceBlock.getLocation().clone();
		double dy = targetDestination.getY() - sourceBlock.getY();
		if (dy <= 2) {
			loc.setY(sourceBlock.getY() + 2);
		} else {
			loc.setY(targetDestination.getY() - 1);
		}
		return loc;
	}

	public void moveWater() {
		if (sourceBlock != null) {
			if (sourceBlock.getWorld().equals(player.getWorld())) {
				targetDestination = getTargetLocation(player, range);

				if (targetDestination.distanceSquared(location) <= 1) {
					progressing = false;
					targetDestination = null;
					remove();
					return;
				} else {
					progressing = true;
					settingUp = true;
					firstDestination = getToEyeLevel();
					firstDirection = GeneralMethods.getDirection(sourceBlock.getLocation(), firstDestination)
							.normalize();
					targetDestination = GeneralMethods.getPointOnLine(firstDestination, targetDestination, range);
					targetDirection = GeneralMethods.getDirection(firstDestination, targetDestination).normalize();

					if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
						new PlantRegrowth(player, sourceBlock);
						sourceBlock.setType(Material.AIR);
					} else if (!isIce(sourceBlock)) {
						addWater(sourceBlock);
					}
				}
			}
			bPlayer.addCooldown(this);
		}
	}

	public boolean prepare() {
		Block block = BlockSource.getWaterSourceBlock(player, selectRange, ClickType.SHIFT_DOWN, true, true,
				bPlayer.canPlantbend());
		cancelPrevious();
		block(player);

		if (block != null) {
			sourceBlock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - time >= interval) {
			if (!progressing && !falling && !bPlayer.getBoundAbilityName().equalsIgnoreCase(getName())) {
				remove();
				return;
			}

			if (falling) {
				remove();
				new WaterReturn(player, sourceBlock);
				return;
			} else {
				if (!progressing) {
					if (!(isWater(sourceBlock.getType()) || (isIce(sourceBlock) && bPlayer.canIcebend()) || (isSnow(sourceBlock) && bPlayer.canIcebend()) || (isPlant(sourceBlock) && bPlayer.canPlantbend()))) {
						remove();
						return;
					}
					sourceBlock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) selectRange);
					return;
				}

				if (sourceBlock.getLocation().distanceSquared(firstDestination) < 0.5 * 0.5) {
					settingUp = false;
				}

				Vector direction;
				if (settingUp) {
					direction = firstDirection;
				} else {
					direction = targetDirection;
				}

				Block block = location.getBlock();
				if (displacing) {
					Block targetBlock = player.getTargetBlock((HashSet<Material>) null, dispelRange);
					direction = GeneralMethods.getDirection(location, targetBlock.getLocation()).normalize();
					if (!location.getBlock().equals(targetBlock.getLocation())) {
						location = location.clone().add(direction);

						block = location.getBlock();
						if (block.getLocation().equals(sourceBlock.getLocation())) {
							location = location.clone().add(direction);
							block = location.getBlock();
						}
					}
				} else {
					WaterAbility.removeWaterSpouts(location, player);
					AirAbility.removeAirSpouts(location, player);
					EarthAbility.removeSandSpouts(location, player);

					if ((new Random()).nextInt(4) == 0) {
						playWaterbendingSound(location);
					}

					double radius = collisionRadius;
					Player source = player;
					if (!(location == null)) {
						if (EarthBlast.annihilateBlasts(location, radius, source)
								|| WaterManipulation.annihilateBlasts(location, radius, source)
								|| FireBlast.annihilateBlasts(location, radius, source)) {
							remove();
							new WaterReturn(player, sourceBlock);
							return;
						}
						Combustion.removeAroundPoint(location, radius);
					}

					location = location.clone().add(direction);
					block = location.getBlock();
					if (block.getLocation().equals(sourceBlock.getLocation())) {
						location = location.clone().add(direction);
						block = location.getBlock();
					}
				}

				if (trail2 != null) {
					if (!TempBlock.isTempBlock(block) && (trail2.getBlock().equals(block))) {
						trail2.revertBlock();
						trail2 = null;
					}
				}

				if (trail != null) {
					if (!TempBlock.isTempBlock(block) && trail.getBlock().equals(block)) {
						trail.revertBlock();
						trail = null;
						if (trail2 != null) {
							trail2.revertBlock();
							trail2 = null;
						}
					}
				}

				if (isTransparent(player, block) && !block.isLiquid()) {
					GeneralMethods.breakBlock(block);
				} else if (block.getType() != Material.AIR && !isWater(block)) {
					remove();
					new WaterReturn(player, sourceBlock);
					return;
				}

				if (!displacing) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, collisionRadius)) {
						if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
							Location location = player.getEyeLocation();
							Vector vector = location.getDirection();
							entity.setVelocity(vector.normalize().multiply(pushFactor));

							if (bPlayer.isAvatarState()) {
								damage = AvatarState.getValue(damage);
							}
							damage = getNightFactor(damage);
							DamageHandler.damageEntity(entity, damage, this);
							AirAbility.breakBreathbendingHold(entity);
							progressing = false;
						}
					}
				}

				if (!progressing) {
					remove();
					new WaterReturn(player, sourceBlock);
					return;
				}

				addWater(block);
				reduceWater(sourceBlock);

				if (trail2 != null) {
					trail2.revertBlock();
					trail2 = null;
				}
				if (trail != null) {
					trail2 = trail;
					trail2.setType(Material.STATIONARY_WATER, (byte) 2);
				}
				trail = new TempBlock(sourceBlock, Material.STATIONARY_WATER, (byte) 1);
				sourceBlock = block;

				if (location.distanceSquared(targetDestination) <= 1
						|| location.distanceSquared(firstDestination) > range * range) {
					falling = true;
					progressing = false;
				}
			}
		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (progressing && !settingUp) {
			if (location.distanceSquared(player.getLocation()) <= range * range) {
				targetDirection = GeneralMethods.getDirection(location, targetlocation).normalize();
			}
			targetDestination = targetlocation;
			this.player = player;
		}
	}

	private void reduceWater(Block block) {
		if (displacing) {
			removeWater(block);
			return;
		}
		if (AFFECTED_BLOCKS.containsKey(block)) {
			if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.AIR);
			}
			AFFECTED_BLOCKS.remove(block);
		}
	}

	private void removeWater(Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
					block.setType(Material.AIR);
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void addWater(Block block) {
		if (!isWater(block)) {
			if (!AFFECTED_BLOCKS.containsKey(block)) {
				AFFECTED_BLOCKS.put(block, block);
			}
			if (PhaseChangeFreeze.getFrozenBlocks().containsKey(block)) {
				PhaseChangeFreeze.getFrozenBlocks().remove(block);
			}
			block.setType(Material.STATIONARY_WATER);
			block.setData((byte) 0);
		} else {
			if (isWater(block) && !AFFECTED_BLOCKS.containsKey(block)) {
				ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f,
						5, block.getLocation().clone().add(.5, .5, .5), 257D);
			} 
		}

	}

	public static boolean annihilateBlasts(Location location, double radius, Player player) {
		boolean broke = false;
		for (WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (manip.location.getWorld().equals(location.getWorld()) && !player.equals(manip.player) && manip.progressing) {
				if (manip.location.distanceSquared(location) <= radius * radius) {
					manip.remove();
					broke = true;
				}
			}
		}
		return broke;
	}

	/**Blocks other water manips*/
	private static void block(Player player) {
		for (WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (!manip.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!manip.progressing) {
				continue;
			} else if (manip.getPlayer().equals(player)) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(manip, manip.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distanceSquared(location) <= manip.selectRange * manip.selectRange
					&& GeneralMethods.getDistanceFromLine(vector, location, manip.location) < manip.deflectRange
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc
							.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				manip.remove();
			}
		}
	}

	public static boolean canBubbleWater(Block block) {
		return canPhysicsChange(block);
	}

	public static boolean canFlowFromTo(Block from, Block to) {
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
		} else if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			return false;
		} else if (isAdjacentToFrozenBlock(to) || isAdjacentToFrozenBlock(from)) {
			return false;
		}
		return true;
	}

	public static boolean canPhysicsChange(Block block) {
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
		} else if (TempBlock.isTempBlock(block)) {
			return false;
		} else if (TempBlock.isTouchingTempBlock(block)) {
			return false;
		}
		return true;
	}

	private static Location getTargetLocation(Player player, double range) {
		Location location;
		Entity target = GeneralMethods.getTargetedEntity(player, range);

		if (target == null) {
			location = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterial());
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
	}

	@SuppressWarnings("deprecation")
	public static void moveWater(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		if (bPlayer.isOnCooldown("WaterManipulation")) {
			redirectTargettedBlasts(player);
			return;
		}

		boolean handledPrepare = false;
		double range = 25;
		for (WaterManipulation waterManip : getAbilities(player, WaterManipulation.class)) {
			range = waterManip.range;
			if (waterManip.prepared) {
				waterManip.prepared = false;
				handledPrepare = true;
				waterManip.moveWater();
			}
		}

		if (!handledPrepare && WaterReturn.hasWaterBottle(player)) {
			Location eyeLoc = player.getEyeLocation();
			Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			if (!AFFECTED_BLOCKS.containsKey(block)) {
				AFFECTED_BLOCKS.put(block, block);
			}

			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				if (getTargetLocation(player, range).distanceSquared(block.getLocation()) > 1) {
					block.setType(Material.WATER);
					block.setData((byte) 0);

					WaterManipulation waterManip = new WaterManipulation(player);
					waterManip.moveWater();
					if (!waterManip.progressing) {
						block.setType(Material.AIR);
					} else {
						WaterReturn.emptyWaterBottle(player);
					}
				}
			}
		}
		redirectTargettedBlasts(player);
	}

	private static void redirectTargettedBlasts(Player player) {
		for (WaterManipulation manip : getAbilities(WaterManipulation.class)) {
			if (!manip.progressing) {
				continue;
			} else if (!manip.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", manip.location)) {
				continue;
			}

			if (manip.player.equals(player)) {
				manip.redirect(player, getTargetLocation(player, manip.range));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distanceSquared(location) <= manip.selectRange * manip.selectRange
					&& GeneralMethods.getDistanceFromLine(vector, location, manip.location) < manip.deflectRange
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc
							.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				manip.redirect(player, getTargetLocation(player, manip.selectRange));
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		finalRemoveWater(sourceBlock);
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (WaterManipulation manip : getAbilities(WaterManipulation.class)) {
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
		if (location != null) {
			return location;
		} else if (sourceBlock != null) {
			return sourceBlock.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isProgressing() {
		return progressing;
	}

	public void setProgressing(boolean progressing) {
		this.progressing = progressing;
	}

	public boolean isFalling() {
		return falling;
	}

	public void setFalling(boolean falling) {
		this.falling = falling;
	}

	public boolean isSettingUp() {
		return settingUp;
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isDisplacing() {
		return displacing;
	}

	public void setDisplacing(boolean displacing) {
		this.displacing = displacing;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}

	public int getDispelRange() {
		return dispelRange;
	}

	public void setDispelRange(int dispelRange) {
		this.dispelRange = dispelRange;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getDeflectRange() {
		return deflectRange;
	}

	public void setDeflectRange(double deflectRange) {
		this.deflectRange = deflectRange;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getTrail() {
		return trail;
	}

	public void setTrail(TempBlock trail) {
		this.trail = trail;
	}

	public TempBlock getTrail2() {
		return trail2;
	}

	public void setTrail2(TempBlock trail2) {
		this.trail2 = trail2;
	}

	public Location getFirstDestination() {
		return firstDestination;
	}

	public void setFirstDestination(Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getTargetDestination() {
		return targetDestination;
	}

	public void setTargetDestination(Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Vector getFirstDirection() {
		return firstDirection;
	}

	public void setFirstDirection(Vector firstDirection) {
		this.firstDirection = firstDirection;
	}

	public Vector getTargetDirection() {
		return targetDirection;
	}

	public void setTargetDirection(Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public HashSet<Byte> getWaterTypes() {
		return waterTypes;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public double getCollisionRadius() {
		return collisionRadius;
	}

	public void setCollisionRadius(double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

}
