package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.firebending.FireBlast;

public class WaterWall {

	public static ConcurrentHashMap<Integer, WaterWall> instances = new ConcurrentHashMap<Integer, WaterWall>();

	private static final long interval = 30;

	public static ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Player> wallblocks = new ConcurrentHashMap<Block, Player>();

	private static final byte full = 0x0;
	// private static final byte half = 0x4;

	private static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
	private static final double defaultradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");
	// private static double speed = 1.5;

	Player player;
	private Location location = null;
	private Block sourceblock = null;
	// private Block oldwater = null;
	private boolean progressing = false;
	private Location firstdestination = null;
	private Location targetdestination = null;
	private Vector firstdirection = null;
	private Vector targetdirection = null;
	// private boolean falling = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean frozen = false;
	private long time;
	private double radius = defaultradius;

	public WaterWall(Player player) {
		this.player = player;

		if (Wave.instances.containsKey(player.getEntityId())) {
			Wave wave = Wave.instances.get(player.getEntityId());
			if (!wave.progressing) {
				Wave.launch(player);
				return;
			}
		}

		if (AvatarState.isAvatarState(player)) {
			radius = AvatarState.getValue(radius);
		}
		if (instances.containsKey(player.getEntityId())) {
			if (instances.get(player.getEntityId()).progressing) {
				freezeThaw(player);
			} else if (prepare()) {
				if (instances.containsKey(player.getEntityId())) {
					instances.get(player.getEntityId()).cancel();
				}
				// Methods.verbose("New water wall prepared");
				instances.put(player.getEntityId(), this);
				time = System.currentTimeMillis();

			}
		} else if (prepare()) {
			if (instances.containsKey(player.getEntityId())) {
				instances.get(player.getEntityId()).cancel();
			}
			// Methods.verbose("New water wall prepared");
			instances.put(player.getEntityId(), this);
			time = System.currentTimeMillis();
		}

		if (Wave.cooldowns.containsKey(player.getName())) {
			if (Wave.cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				Wave.cooldowns.remove(player.getName());
			}
		}

		if (!instances.containsKey(player.getEntityId())
				&& WaterReturn.hasWaterBottle(player)) {

			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (Methods.isTransparentToEarthbending(player, block)
					&& Methods.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				Wave wave = new Wave(player);
				wave.canhitself = false;
				wave.moveWater();
				if (!wave.progressing) {
					block.setType(Material.AIR);
					wave.cancel();
				} else {
					WaterReturn.emptyWaterBottle(player);
				}
			}

		}

	}

	private static void freezeThaw(Player player) {
		instances.get(player.getEntityId()).freezeThaw();
	}

	private void freezeThaw() {
		if (frozen) {
			thaw();
		} else {
			freeze();
		}
	}

	private void freeze() {
		frozen = true;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				new TempBlock(block, Material.ICE, (byte) 0);
			}
		}
	}

	private void thaw() {
		frozen = false;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				new TempBlock(block, Material.WATER, full);
			}
		}
	}

	public boolean prepare() {
		cancelPrevious();
		// Block block = player.getTargetBlock(null, (int) range);
		Block block = Methods.getWaterSourceBlock(player, range,
				Methods.canPlantbend(player));
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(player.getEntityId())) {
			WaterWall old = instances.get(player.getEntityId());
			if (old.progressing) {
				old.removeWater(old.sourceblock);
			} else {
				old.cancel();
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
		instances.remove(player.getEntityId());
	}

	public void moveWater() {
		if (sourceblock != null) {
			targetdestination = player.getTargetBlock(
					Methods.getTransparentEarthbending(), (int) range)
					.getLocation();

			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
			} else {
				progressing = true;
				settingup = true;
				firstdestination = getToEyeLevel();
				firstdirection = getDirection(sourceblock.getLocation(),
						firstdestination);
				targetdirection = getDirection(firstdestination,
						targetdestination);
				if (Methods.isPlant(sourceblock))
					new Plantbending(sourceblock);
				if (!Methods.isAdjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addWater(sourceblock);
			}

		}
	}

	private Location getToEyeLevel() {
		Location loc = sourceblock.getLocation().clone();
		loc.setY(targetdestination.getY());
		return loc;
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			breakBlock();
			// instances.remove(player.getEntityId());
			return false;
		}
		if (!Methods.canBend(player.getName(), "Surge")) {
			if (!forming)
				// removeWater(oldwater);
				breakBlock();
			unfocusBlock();
			returnWater();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!forming) {
				// removeWater(oldwater);
			}

			if (Methods.getBoundAbility(player) == null) {
				unfocusBlock();
				return false;
			}
			if (!progressing
					&& !Methods.getBoundAbility(player).equalsIgnoreCase("Surge")) {
				unfocusBlock();
				return false;
			}

			if (progressing
					&& (!player.isSneaking() || !Methods.getBoundAbility(player).equalsIgnoreCase("Surge"))) {
				breakBlock();
				returnWater();
				return false;
			}

			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4,
						(int) range);
				return false;
			}

			if (forming) {
				ArrayList<Block> blocks = new ArrayList<Block>();
				Location loc = Methods.getTargetedLocation(player, (int) range,
						8, 9, 79);
				location = loc.clone();
				Vector dir = player.getEyeLocation().getDirection();
				Vector vec;
				Block block;
				for (double i = 0; i <= Methods.waterbendingNightAugment(radius,
						player.getWorld()); i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						// loc.getBlock().setType(Material.GLOWSTONE);
						vec = Methods.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();
//						if (Methods.isRegionProtectedFromBuild(player,
//								Abilities.Surge, block.getLocation()))
//							continue;
						if (wallblocks.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block)
								&& (block.getType() == Material.AIR
								|| block.getType() == Material.FIRE || Methods
								.isWaterbendable(block, player))) {
							wallblocks.put(block, player);
							addWallBlock(block);
							// if (frozen) {
							// block.setType(Material.ICE);
							// } else {
							// block.setType(Material.WATER);
							// block.setData(full);
							// }
							// block.setType(Material.GLASS);
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(
									block.getLocation(), 2);
							// Methods.verbose(wallblocks.size());
						}
					}
				}

				for (Block blocki : wallblocks.keySet()) {
					if (wallblocks.get(blocki) == player
							&& !blocks.contains(blocki)) {
						finalRemoveWater(blocki);
					}
				}

				return true;
			}

			if (sourceblock.getLocation().distance(firstdestination) < .5
					&& settingup) {
				settingup = false;
			}

			Vector direction;
			if (settingup) {
				direction = firstdirection;
			} else {
				direction = targetdirection;
			}

			location = location.clone().add(direction);

			Block block = location.getBlock();
			if (block.getLocation().equals(sourceblock.getLocation())) {
				location = location.clone().add(direction);
				block = location.getBlock();
			}
			if (block.getType() != Material.AIR) {
				breakBlock();
				returnWater();
				return false;
			}

			if (!progressing) {
				breakBlock();
				return false;
			}

			addWater(block);
			removeWater(sourceblock);
			sourceblock = block;

			if (location.distance(targetdestination) < 1) {

				removeWater(sourceblock);
				// removeWater(oldwater);
				forming = true;
			}

			return true;
		}

		return false;

	}

	private void addWallBlock(Block block) {
		if (frozen) {
			new TempBlock(block, Material.ICE, (byte) 0);
		} else {
			new TempBlock(block, Material.WATER, full);
		}
	}

	private void breakBlock() {
		finalRemoveWater(sourceblock);
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				finalRemoveWater(block);
			}
		}
		instances.remove(player.getEntityId());
	}

	// private void reduceWater(Block block) {
	// if (affectedblocks.containsKey(block)) {
	// if (!Methods.adjacentToThreeOrMoreSources(block)) {
	// block.setType(Material.WATER);
	// block.setData(half);
	// }
	// oldwater = block;
	// }
	// }

	private void removeWater(Block block) {
		if (block != null) {
			if (affectedblocks.containsKey(block)) {
				if (!Methods.isAdjacentToThreeOrMoreSources(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				affectedblocks.remove(block);
			}
		}
	}

	private static void finalRemoveWater(Block block) {
		if (affectedblocks.containsKey(block)) {
			// block.setType(Material.WATER);
			// block.setData(half);
			// if (!Methods.adjacentToThreeOrMoreSources(block)) {
			// block.setType(Material.AIR);
			// }
			TempBlock.revertBlock(block, Material.AIR);
			affectedblocks.remove(block);
		}

		if (wallblocks.containsKey(block)) {
			// if (block.getType() == Material.ICE
			// || block.getType() == Material.WATER
			// || block.getType() == Material.STATIONARY_WATER) {
			// block.setType(Material.AIR);
			// }
			TempBlock.revertBlock(block, Material.AIR);
			wallblocks.remove(block);
			// block.setType(Material.WATER);
			// block.setData(half);
		}
	}

	private void addWater(Block block) {

//		if (Methods.isRegionProtectedFromBuild(player, Abilities.Surge,
//				block.getLocation()))
//			return;

		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.WATER, full);
			// new TempBlock(block, Material.ICE, (byte) 0);
			affectedblocks.put(block, block);
		}

		// if (!affectedblocks.containsKey(block)) {
		// affectedblocks.put(block, block);
		// }
		// if (FreezeMelt.frozenblocks.containsKey(block))
		// FreezeMelt.frozenblocks.remove(block);
		// block.setType(Material.WATER);
		// block.setData(full);
	}

	public static void moveWater(Player player) {
		if (instances.containsKey(player.getEntityId())) {
			instances.get(player.getEntityId()).moveWater();
		}
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static void form(Player player) {

		if (!instances.containsKey(player.getEntityId())) {
			if (!Wave.instances.containsKey(player.getEntityId())
					&& Methods.getWaterSourceBlock(player,
							(int) Wave.defaultrange, Methods.canPlantbend(player)) == null
							&& WaterReturn.hasWaterBottle(player)) {

				if (Wave.cooldowns.containsKey(player.getName())) {
					if (Wave.cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
						return;
					} else {
						Wave.cooldowns.remove(player.getName());
					}
				}

				Location eyeloc = player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize())
						.getBlock();
				if (Methods.isTransparentToEarthbending(player, block)
						&& Methods.isTransparentToEarthbending(player,
								eyeloc.getBlock())) {
					block.setType(Material.WATER);
					block.setData(full);
					WaterWall wall = new WaterWall(player);
					wall.moveWater();
					if (!wall.progressing) {
						block.setType(Material.AIR);
						wall.cancel();
					} else {
						WaterReturn.emptyWaterBottle(player);
					}
					return;
				}
			}

			new Wave(player);
			return;
		} else {
			if (Methods.isWaterbendable(
					player.getTargetBlock(null, (int) Wave.defaultrange),
					player)) {
				new Wave(player);
				return;
			}
		}

		moveWater(player);
	}

	public static void removeAll() {
		for (Block block : affectedblocks.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			affectedblocks.remove(block);
			wallblocks.remove(block);
		}
		for (Block block : wallblocks.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			affectedblocks.remove(block);
			wallblocks.remove(block);
		}
	}

	public static boolean canThaw(Block block) {
		if (wallblocks.keySet().contains(block))
			return false;
		return true;
	}

	public static void thaw(Block block) {
		finalRemoveWater(block);
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player.getEntityId())) {
			WaterWall wall = instances.get(player.getEntityId());
			if (wall.sourceblock == null)
				return false;
			if (wall.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void returnWater() {
		if (location != null) {
			new WaterReturn(player, location.getBlock());
		}
	}

	public static String getDescription() {
		return "This ability has two distinct features. If you sneak to select a source block, "
				+ "you can then click in a direction and a large wave will be launched in that direction. "
				+ "If you sneak again while the wave is en route, the wave will freeze the next target it hits. "
				+ "If, instead, you click to select a source block, you can hold sneak to form a wall of water at "
				+ "your cursor location. Click to shift between a water wall and an ice wall. "
				+ "Release sneak to dissipate it.";
	}

}