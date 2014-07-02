package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.firebending.FireBlast;

public class WaterManipulation {

	public static ConcurrentHashMap<Integer, WaterManipulation> instances = new ConcurrentHashMap<Integer, WaterManipulation>();
	public static ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Player, Integer> prepared = new ConcurrentHashMap<Player, Integer>();

	private static int ID = Integer.MIN_VALUE;

	private static final byte full = 0x0;
	// private static final byte half = 0x4;

	static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterManipulation.Range");
	private static double pushfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterManipulation.Push");
	private static double defaultdamage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterManipulation.Damage");
	private static double speed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterManipulation.Speed");
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown");
	private static Map<String, Long> cooldowns = new HashMap<String, Long>();
	private static final double deflectrange = 3;
	// private static double speed = 1.5;

	private static HashSet<Byte> water = new HashSet<Byte>();

	private static long interval = (long) (1000. / speed);

	Player player;
	private int id;
	private Location location = null;
	private Block sourceblock = null;
	private TempBlock trail, trail2;
	private boolean progressing = false;
	private Location firstdestination = null;
	private Location targetdestination = null;
	private Vector firstdirection = null;
	private Vector targetdirection = null;
	private boolean falling = false;
	private boolean settingup = false;
	// private boolean targetting = false;
	private final boolean displacing = false;
	private long time;
	private double damage = defaultdamage;
	private int displrange;

	public WaterManipulation(Player player) {
		if (water.isEmpty()) {
			water.add((byte) 0);
			water.add((byte) 8);
			water.add((byte) 9);
		}
		this.player = player;
		if (prepare()) {
			id = ID;
			instances.put(id, this);
			prepared.put(player, id);
			if (ID == Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			ID++;
			time = System.currentTimeMillis();
		}
	}

	public boolean prepare() {
		// Block block = player.getTargetBlock(null, (int) range);
		Block block = Methods.getWaterSourceBlock(player, range,
				Methods.canPlantbend(player));
		// if (prepared.containsKey(player)
		// && !Methods.isWaterbendable(block, player)) {
		// instances.get(prepared.get(player)).displacing = true;
		// instances.get(prepared.get(player)).moveWater();
		// }
		cancelPrevious();
		block(player);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		if (prepared.containsKey(player)) {
			if (instances.containsKey(prepared.get(player))) {
				WaterManipulation old = instances.get(prepared.get(player));
				if (!old.progressing) {
					old.cancel();
				}
			}
		}
	}

	public void cancel() {
		unfocusBlock();
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	private void unfocusBlock() {
		remove(id);
	}

	public void moveWater() {
		if (sourceblock != null) {
			if (sourceblock.getWorld() == player.getWorld()) {
				targetdestination = getTargetLocation(player);

				if (targetdestination.distance(location) <= 1) {
					progressing = false;
					targetdestination = null;
					remove(id);
				} else {
					progressing = true;
					settingup = true;
					firstdestination = getToEyeLevel();
					firstdirection = Methods.getDirection(
							sourceblock.getLocation(), firstdestination)
							.normalize();
					targetdestination = Methods.getPointOnLine(firstdestination,
							targetdestination, range);
					targetdirection = Methods.getDirection(firstdestination,
							targetdestination).normalize();

					if (Methods.isPlant(sourceblock))
						new Plantbending(sourceblock);
					addWater(sourceblock);
				}

			}
			
			cooldowns.put(player.getName(), System.currentTimeMillis());

		}
	}

	private static Location getTargetLocation(Player player) {
		Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
		Location location;
		if (target == null) {
			location = Methods.getTargetedLocation(player, range,
					Methods.transparentToEarthbending);
		} else {
			// targetting = true;
			location = ((LivingEntity) target).getEyeLocation();
			// location.setY(location.getY() - 1);
		}
		return location;
	}

	private Location getToEyeLevel() {
		Location loc = sourceblock.getLocation().clone();
		double dy = targetdestination.getY() - sourceblock.getY();
		if (dy <= 2) {
			loc.setY(sourceblock.getY() + 2);
		} else {
			loc.setY(targetdestination.getY() - 1);
		}
		return loc;
	}

	private static void remove(int id) {
		Player player = instances.get(id).player;
		if (prepared.containsKey(player)) {
			if (prepared.get(player) == id)
				prepared.remove(player);
		}
		instances.remove(id);
	}

	private void redirect(Player player, Location targetlocation) {
		if (progressing && !settingup) {
			if (location.distance(player.getLocation()) <= range)
				targetdirection = Methods.getDirection(location, targetlocation)
				.normalize();
			targetdestination = targetlocation;
			this.player = player;
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()
				|| !Methods.canBend(player.getName(), "WaterManipulation")) {
			breakBlock();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			// removeWater(oldwater);
			if (Methods.isRegionProtectedFromBuild(player,
					"WaterManipulation", location)) {
				breakBlock();
				return false;
			}

			time = System.currentTimeMillis();

			if (Methods.getBoundAbility(player) == null) {
				unfocusBlock();
				return false;
			}
			if (!progressing
					&& !falling
					&& !Methods.getBoundAbility(player).equalsIgnoreCase("WaterManipulation")) {
				unfocusBlock();
				return false;
			}

			if (falling) {
				// location = location.clone().add(0, -1, 0);
				//
				// if (location.getBlock().getType() != Material.AIR) {
				// falling = false;
				// unfocusBlock();
				// return false;
				// }
				//
				// for (Entity entity : Methods.getEntitiesAroundPoint(location,
				// 1)) {
				// if (entity instanceof LivingEntity) {
				// Methods.damageEntity(player, entity, damage);
				// falling = false;
				// }
				// }
				//
				// if (!falling) {
				// breakBlock();
				// return false;
				// }
				//
				// location.getBlock().setType(sourceblock.getType());
				// sourceblock.setType(Material.AIR);
				//
				// sourceblock = location.getBlock();
				// if (!Methods.isSolid(sourceblock.getRelative(BlockFace.DOWN))
				// || targetting) {
				// finalRemoveWater(sourceblock);
				// } else {
				// sourceblock.setData(full);
				// affectedblocks.remove(sourceblock);
				// }
				//
				// instances.remove(player.getEntityId());
				breakBlock();
				new WaterReturn(player, sourceblock);
				return false;

			} else {
				if (!progressing) {
					sourceblock.getWorld().playEffect(location, Effect.SMOKE,
							4, (int) range);
					return false;
				}

				// Methods.verbose(firstdestination);

				if (sourceblock.getLocation().distance(firstdestination) < .5) {
					settingup = false;
				}

				// if (!player.isSneaking() && displacing) {
				// displacing = false;
				// breakBlock();
				// return false;
				// }

				Vector direction;
				if (settingup) {
					direction = firstdirection;
				} else {
					direction = targetdirection;
				}

				Block block = location.getBlock();
				if (displacing) {
					Block targetblock = player.getTargetBlock(null, displrange);
					direction = Methods.getDirection(location,
							targetblock.getLocation()).normalize();
					if (!location.getBlock().equals(targetblock.getLocation())) {
						location = location.clone().add(direction);

						block = location.getBlock();
						if (block.getLocation().equals(
								sourceblock.getLocation())) {
							location = location.clone().add(direction);
							block = location.getBlock();
						}
					}

				} else {
					Methods.removeSpouts(location, player);

					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.annihilateBlasts(location,
									radius, source)
									|| FireBlast.annihilateBlasts(location, radius,
											source)) {
						breakBlock();
						new WaterReturn(player, sourceblock);
						return false;
					}

					location = location.clone().add(direction);

					block = location.getBlock();
					if (block.getLocation().equals(sourceblock.getLocation())) {
						location = location.clone().add(direction);
						block = location.getBlock();
					}
				}

				if (trail2 != null) {
					if (trail2.getBlock().equals(block)) {
						trail2.revertBlock();
						trail2 = null;
					}
				}

				if (trail != null) {
					if (trail.getBlock().equals(block)) {
						trail.revertBlock();
						trail = null;
						if (trail2 != null) {
							trail2.revertBlock();
							trail2 = null;
						}
					}
				}

				if (Methods.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					Methods.breakBlock(block);
				} else if (block.getType() != Material.AIR
						&& !Methods.isWater(block)) {
					breakBlock();
					new WaterReturn(player, sourceblock);
					return false;
				}

				if (!displacing) {
					for (Entity entity : Methods.getEntitiesAroundPoint(location,
							FireBlast.affectingradius)) {
						if (entity instanceof LivingEntity
								&& entity.getEntityId() != player.getEntityId()) {

							// Block testblock = location.getBlock();
							// Block block1 = entity.getLocation().getBlock();
							// Block block2 = ((LivingEntity) entity)
							// .getEyeLocation().getBlock();
							//
							// if (testblock.equals(block1)
							// || testblock.equals(block2)) {

							Location location = player.getEyeLocation();
							Vector vector = location.getDirection();
							entity.setVelocity(vector.normalize().multiply(pushfactor));
							// entity.setVelocity(entity.getVelocity().clone()
							// .add(direction));
							if (AvatarState.isAvatarState(player))
								damage = AvatarState.getValue(damage);
							Methods.damageEntity(player, entity, (int) Methods
									.waterbendingNightAugment(damage,
											player.getWorld()));
							progressing = false;
							// }
						}
					}
				}

				if (!progressing) {
					breakBlock();
					new WaterReturn(player, sourceblock);
					return false;
				}

				addWater(block);
				reduceWater(sourceblock);
				// if (block.getType() != Material.AIR) {
				// block.setType(Material.GLOWSTONE);
				// } else {
				// block.setType(Material.GLASS);
				// }

				if (trail2 != null) {
					trail2.revertBlock();
					trail2 = null;
				}
				if (trail != null) {
					trail2 = trail;
					trail2.setType(Material.WATER, (byte) 2);
				}
				trail = new TempBlock(sourceblock, Material.WATER, (byte) 1);
				sourceblock = block;

				if (location.distance(targetdestination) <= 1
						|| location.distance(firstdestination) > range) {

					falling = true;
					progressing = false;
				}

				return true;
			}
		}

		return false;

	}

	private void breakBlock() {

		// removeWater(oldwater);
		finalRemoveWater(sourceblock);
		remove(id);
	}

	private void reduceWater(Block block) {
		if (displacing) {
			removeWater(block);
			return;
		}
		if (affectedblocks.containsKey(block)) {
			if (!Methods.isAdjacentToThreeOrMoreSources(block)) {
				// && !Methods.adjacentToAnyWater(block)) {
				block.setType(Material.AIR);
				// block.setType(Material.WATER);
				// block.setData(half);
			}
			// oldwater = block;
			affectedblocks.remove(block);
		}
	}

	private void removeWater(Block block) {
		if (block != null) {
			if (affectedblocks.containsKey(block)) {
				if (!Methods.isAdjacentToThreeOrMoreSources(block)) {
					block.setType(Material.AIR);
				}
				affectedblocks.remove(block);
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
		if (affectedblocks.containsKey(block)) {
			if (!Methods.isAdjacentToThreeOrMoreSources(block)) {
				// && !Methods.adjacentToAnyWater(block)) {
				block.setType(Material.AIR);
				// block.setType(Material.WATER);
				// block.setData(half);
			}
			affectedblocks.remove(block);
		}
	}

	private static void addWater(Block block) {
		if (!affectedblocks.containsKey(block)) {
			affectedblocks.put(block, block);
		}
		if (FreezeMelt.frozenblocks.containsKey(block))
			FreezeMelt.frozenblocks.remove(block);
		block.setType(Material.WATER);
		block.setData(full);
	}

	public static void moveWater(Player player) {
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		if (prepared.containsKey(player)) {
			if (instances.containsKey(prepared.get(player))) {
				instances.get(prepared.get(player)).moveWater();
			}
			prepared.remove(player);
		} else if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (Methods.isTransparentToEarthbending(player, block)
					&& Methods.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {

				if (getTargetLocation(player).distance(block.getLocation()) > 1) {
					block.setType(Material.WATER);
					block.setData(full);
					WaterManipulation watermanip = new WaterManipulation(
							player);
					watermanip.moveWater();
					if (!watermanip.progressing) {
						block.setType(Material.AIR);
					} else {
						WaterReturn.emptyWaterBottle(player);
					}
				}
			}
		}
		//		}

		redirectTargettedBlasts(player);
	}

	private static void redirectTargettedBlasts(Player player) {
		for (int id : instances.keySet()) {
			WaterManipulation manip = instances.get(id);

			if (!manip.progressing)
				continue;

			if (!manip.location.getWorld().equals(player.getWorld()))
				continue;

			if (Methods.isRegionProtectedFromBuild(player,
					"WaterManipulation", manip.location))
				continue;

			if (manip.player.equals(player))
				manip.redirect(player, getTargetLocation(player));

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= range
					&& Methods.getDistanceFromLine(vector, location,
							manip.location) < deflectrange
							&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				manip.redirect(player, getTargetLocation(player));
			}

		}
	}

	private static void block(Player player) {
		for (int id : instances.keySet()) {
			WaterManipulation manip = instances.get(id);

			if (manip.player.equals(player))
				continue;

			if (!manip.location.getWorld().equals(player.getWorld()))
				continue;

			if (!manip.progressing)
				continue;

			if (Methods.isRegionProtectedFromBuild(player,
					"WaterManipulation", manip.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= range
					&& Methods.getDistanceFromLine(vector, location,
							manip.location) < deflectrange
							&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				manip.breakBlock();
			}

		}
	}

	public static boolean progress(int ID) {
		if (instances.containsKey(ID))
			return instances.get(ID).progress();
		return false;
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		// if (to.getType() == Material.TORCH)
		// return true;
		if (affectedblocks.containsKey(to) || affectedblocks.containsKey(from)) {
			// Methods.verbose("affectedblocks");
			return false;
		}
		if (WaterSpout.affectedblocks.containsKey(to)
				|| WaterSpout.affectedblocks.containsKey(from)) {
			// Methods.verbose("waterspout");
			return false;
		}
		if (WaterWall.affectedblocks.containsKey(to)
				|| WaterWall.affectedblocks.containsKey(from)) {
			// Methods.verbose("waterwallaffectedblocks");
			return false;
		}
		if (WaterWall.wallblocks.containsKey(to)
				|| WaterWall.wallblocks.containsKey(from)) {
			// Methods.verbose("waterwallwall");
			return false;
		}
		if (Wave.isBlockWave(to) || Wave.isBlockWave(from)) {
			// Methods.verbose("wave");
			return false;
		}
		if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			// Methods.verbose("tempblock");
			return false;
		}
		if (Methods.isAdjacentToFrozenBlock(to)
				|| Methods.isAdjacentToFrozenBlock(from)) {
			// Methods.verbose("frozen");
			return false;
		}

		return true;
	}

	public static boolean canPhysicsChange(Block block) {
		if (affectedblocks.containsKey(block))
			return false;
		if (WaterSpout.affectedblocks.containsKey(block))
			return false;
		if (WaterWall.affectedblocks.containsKey(block))
			return false;
		if (WaterWall.wallblocks.containsKey(block))
			return false;
		if (Wave.isBlockWave(block))
			return false;
		if (TempBlock.isTempBlock(block))
			return false;
		if (TempBlock.isTouchingTempBlock(block))
			return false;
		return true;
	}

	public static void removeAll() {
		for (int id : instances.keySet())
			instances.get(id).breakBlock();
		prepared.clear();
	}

	public static boolean canBubbleWater(Block block) {
		return canPhysicsChange(block);
	}

	public static String getDescription() {
		// TODO Auto-generated method stub
		return "To use, place your cursor over a waterbendable object and tap sneak (default: shift). "
		+ "Smoke will appear where you've selected, indicating the origin of your ability. "
		+ "After you have selected an origin, simply left-click in any direction and you will "
		+ "see your water spout off in that direction, slicing any creature in its path. "
		+ "If you look towards a creature when you use this ability, it will target that creature. "
		+ "A collision from Water Manipulation both knocks the target back and deals some damage. "
		+ "Alternatively, if you have source selected and tap shift again, "
		+ "you will be able to control the water more directly.";
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (int id : instances.keySet()) {
			WaterManipulation manip = instances.get(id);
			if (manip.location.getWorld().equals(location.getWorld()))
				if (manip.location.distance(location) <= radius)
					manip.breakBlock();
		}
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		boolean broke = false;
		for (int id : instances.keySet()) {
			WaterManipulation manip = instances.get(id);
			if (manip.location.getWorld().equals(location.getWorld())
					&& !source.equals(manip.player))
				if (manip.location.distance(location) <= radius) {
					manip.breakBlock();
					broke = true;
				}
		}
		return broke;
	}

}