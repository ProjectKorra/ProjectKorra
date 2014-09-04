package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class EarthBlast {

	public static ConcurrentHashMap<Integer, EarthBlast> instances = new ConcurrentHashMap<Integer, EarthBlast>();

	private static boolean hitself = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthBlast.CanHitSelf");
	private static double preparerange = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.PrepareRange");
	private static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Range");
	private static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.EarthBlast.Damage");
	private static double speed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Speed");
	private static final double deflectrange = 3;

	private static boolean revert = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthBlast.Revert");
	private static double pushfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Push");

	private static long interval = (long) (1000. / speed);

	private static int ID = Integer.MIN_VALUE;

	private Player player;
	private int id;
	private Location location = null;
	private Block sourceblock = null;
	private Material sourcetype = null;
	private boolean progressing = false;
	private Location destination = null;
	private Location firstdestination = null;
	// private Vector firstdirection = null;
	// private Vector targetdirection = null;
	private boolean falling = false;
	private long time;
	private boolean settingup = true;

	public EarthBlast(Player player) {
		this.player = player;
		if (prepare()) {
			// if (instances.containsKey(player.getEntityId())) {
			// instances.get(player.getEntityId()).cancel();
			// }
			id = ID++;
			if (ID >= Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			instances.put(id, this);
			// prepared.put(player, this);
			time = System.currentTimeMillis();
		}

	}

	public boolean prepare() {
		cancelPrevious();
		Block block = Methods.getEarthSourceBlock(player, preparerange);
		// if (Methods.isEarthbendable(player, block)) {
		// sourceblock = block;
		// focusBlock();
		// return true;
		// }
		// return false;
		block(player);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private static Location getTargetLocation(Player player) {
		Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
		Location location;
		if (target == null) {
			location = Methods.getTargetedLocation(player, range);
		} else {
			// targetting = true;
			location = ((LivingEntity) target).getEyeLocation();
			// location.setY(location.getY() - 1);
		}
		return location;
	}

	private void cancelPrevious() {
		// if (prepared.containsKey(player)) {
		// EarthBlast old = prepared.get(player);
		// if (!old.progressing) {
		// old.cancel();
		// }
		// }

		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.player == player && !blast.progressing)
				blast.cancel();
		}

	}

	public void cancel() {
		unfocusBlock();
	}

	private void focusBlock() {
		if (EarthPassive.isPassiveSand(sourceblock))
			EarthPassive.revertSand(sourceblock);
		if (sourceblock.getType() == Material.SAND) {
			sourceblock.setType(Material.SANDSTONE);
			sourcetype = Material.SAND;
		} else if (sourceblock.getType() == Material.STONE) {
			sourceblock.setType(Material.COBBLESTONE);
			sourcetype = Material.STONE;
		} else {
			sourcetype = sourceblock.getType();
			sourceblock.setType(Material.STONE);
		}

		location = sourceblock.getLocation();
	}

	private void unfocusBlock() {
		sourceblock.setType(sourcetype);
		instances.remove(id);
	}

	public void throwEarth() {
		if (sourceblock != null) {
			if (sourceblock.getWorld() == player.getWorld()) {
				if (Methods.movedearth.containsKey(sourceblock)) {
					if (!revert)
						Methods.removeRevertIndex(sourceblock);
					// Methods.removeEarthbendedBlockIndex(sourceblock);
				}
				Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
				// Methods.verbose(target);
				if (target == null) {
					destination = player.getTargetBlock(
							Methods.getTransparentEarthbending(), (int) range)
							.getLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
				} else {
					destination = ((LivingEntity) target).getEyeLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
					destination = Methods.getPointOnLine(firstdestination,
							destination, range);
				}
				// firstdestination.getBlock().setType(Material.GLOWSTONE);
				// destination.getBlock().setType(Material.GLOWSTONE);
				if (destination.distance(location) <= 1) {
					progressing = false;
					destination = null;
				} else {
					progressing = true;
					sourceblock.getWorld().playEffect(
							sourceblock.getLocation(), Effect.GHAST_SHOOT, 0,
							10);
					// direction = getDirection().normalize();
					if (sourcetype != Material.SAND
							&& sourcetype != Material.GRAVEL) {
						sourceblock.setType(sourcetype);
					}
				}
			}

		}
	}

	// private Vector getDirection() {
	// double x1, y1, z1;
	// double x0, y0, z0;
	//
	// x1 = destination.getX();
	// y1 = destination.getY();
	// z1 = destination.getZ();
	//
	// x0 = (double) sourceblock.getX();
	// y0 = (double) sourceblock.getY();
	// z0 = (double) sourceblock.getZ();
	//
	// return new Vector(x1 - x0, y1 - y0, z1 - z0);
	//
	// }

	public static EarthBlast getBlastFromSource(Block block) {
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.sourceblock.equals(block))
				return blast;
		}
		return null;
	}

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()
				|| !Methods.canBend(player.getName(), "EarthBlast")) {
			breakBlock();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (falling) {
				breakBlock();
				return false;
			}

			if (!Methods.isEarthbendable(player, sourceblock)
					&& sourceblock.getType() != Material.COBBLESTONE) {
				instances.remove(id);
				return false;
			}

			if (!progressing && !falling) {

				if (Methods.getBoundAbility(player) == null) {
					unfocusBlock();
					return false;
				}

				if (!Methods.getBoundAbility(player).equalsIgnoreCase("EarthBlast")) {
					unfocusBlock();
					return false;
				}

				if (sourceblock == null) {
					instances.remove(player.getEntityId());
					return false;
				}
				if (player.getWorld() != sourceblock.getWorld()) {
					unfocusBlock();
					return false;
				}
				if (sourceblock.getLocation().distance(player.getLocation()) > preparerange) {
					unfocusBlock();
					return false;
				}
				// if (sourceblock.getType() == Material.AIR) {
				// instances.remove(player.getEntityId());
				// return false;
				// }
			}

			if (falling) {
				// location = location.clone().add(0, -1, 0);
				//
				// if (location.getBlock().getType() == Material.SNOW
				// || Methods.isPlant(location.getBlock())) {
				// Methods.breakBlock(location.getBlock());
				// } else if (location.getBlock().getType() != Material.AIR) {
				// falling = false;
				// unfocusBlock();
				// return false;
				// }
				//
				// for (Entity entity : Methods.getEntitiesAroundPoint(location,
				// 1)) {
				// if (entity instanceof LivingEntity
				// && (entity.getEntityId() != player.getEntityId() || hitself))
				// {
				// Methods.damageEntity(player, entity, damage);
				// falling = false;
				//
				// }
				// }
				//
				// if (!falling) {
				// breakBlock();
				// return false;
				// }
				//
				// if (revert) {
				// Methods.addTempEarthBlock(sourceblock, location.getBlock());
				// }
				//
				// location.getBlock().setType(sourceblock.getType());
				// sourceblock.setType(Material.AIR);
				//
				// sourceblock = location.getBlock();
				//
				// return true;
				breakBlock();

			} else {
				if (!progressing) {
					return false;
				}

				if (sourceblock.getY() == firstdestination.getBlockY())
					settingup = false;

				Vector direction;
				if (settingup) {
					direction = Methods.getDirection(location, firstdestination)
							.normalize();
				} else {
					direction = Methods.getDirection(location, destination)
							.normalize();
				}

				location = location.clone().add(direction);

				Methods.removeSpouts(location, player);

				Block block = location.getBlock();
				if (block.getLocation().equals(sourceblock.getLocation())) {
					location = location.clone().add(direction);
					block = location.getBlock();
				}

				if (Methods.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					Methods.breakBlock(block);
				} else if (!settingup) {
					breakBlock();
					return false;
				} else {
					location = location.clone().subtract(direction);
					direction = Methods.getDirection(location, destination)
							.normalize();
					location = location.clone().add(direction);

					Methods.removeSpouts(location, player);
					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.annihilateBlasts(location,
									radius, source)
									|| FireBlast.annihilateBlasts(location, radius,
											source)) {
						breakBlock();
						return false;
					}

					Combustion.removeAroundPoint(location, radius);

					Block block2 = location.getBlock();
					if (block2.getLocation().equals(sourceblock.getLocation())) {
						location = location.clone().add(direction);
						block2 = location.getBlock();
					}

					if (Methods.isTransparentToEarthbending(player, block)
							&& !block.isLiquid()) {
						Methods.breakBlock(block);
					} else {
						breakBlock();
						return false;
					}
				}

				for (Entity entity : Methods.getEntitiesAroundPoint(location,
						FireBlast.affectingradius)) {
					if (Methods.isRegionProtectedFromBuild(player,
							"EarthBlast", entity.getLocation()))
						continue;
					if (entity instanceof LivingEntity
							&& (entity.getEntityId() != player.getEntityId() || hitself)) {
						// Block testblock = location.getBlock();
						// Block block1 = entity.getLocation().getBlock();
						// Block block2 = ((LivingEntity)
						// entity).getEyeLocation()
						// .getBlock();
						//
						// if (testblock.equals(block1)
						// || testblock.equals(block2)) {
						// entity.setVelocity(entity.getVelocity().clone()
						// .add(direction));

						Methods.breakBreathbendingHold(entity);

						Location location = player.getEyeLocation();
						Vector vector = location.getDirection();
						entity.setVelocity(vector.normalize().multiply(pushfactor));
						Methods.damageEntity(player, entity, damage);
						progressing = false;

						// }
					}
				}

				if (!progressing) {
					breakBlock();
					return false;
				}

				if (revert) {
					// Methods.addTempEarthBlock(sourceblock, block);
					sourceblock.setType(sourcetype);
					Methods.moveEarthBlock(sourceblock, block);
					if (block.getType() == Material.SAND)
						block.setType(Material.SANDSTONE);
					if (block.getType() == Material.GRAVEL)
						block.setType(Material.STONE);
				} else {
					block.setType(sourceblock.getType());
					sourceblock.setType(Material.AIR);
				}

				// block.setType(sourceblock.getType());
				// sourceblock.setType(Material.AIR);
				// if (block.getType() != Material.AIR) {
				// block.setType(Material.GLOWSTONE);
				// } else {
				// block.setType(Material.GLASS);
				// }
				sourceblock = block;

				if (location.distance(destination) < 1) {
					if (sourcetype == Material.SAND
							|| sourcetype == Material.GRAVEL) {
						progressing = false;
						sourceblock.setType(sourcetype);
					}

					falling = true;
					progressing = false;
				}

				return true;
			}
		}

		return false;

	}

	private void breakBlock() {
		sourceblock.setType(sourcetype);
		if (revert) {
			Methods.addTempAirBlock(sourceblock);
		} else {
			sourceblock.breakNaturally();
		}

		instances.remove(id);
	}

	public static void throwEarth(Player player) {
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();

		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("EarthBlast")) return;

		boolean cooldown = false;
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.player == player && !blast.progressing) {
				blast.throwEarth();
				cooldown = true;
				ignore.add(blast);
			}
		}

		if (cooldown)
			bPlayer.addCooldown("EarthBlast", Methods.getGlobalCooldown());

		redirectTargettedBlasts(player, ignore);
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.get(id).breakBlock();
		}
	}

	private static void redirectTargettedBlasts(Player player,
			ArrayList<EarthBlast> ignore) {
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);

			if (!blast.progressing || ignore.contains(blast))
				continue;

			if (!blast.location.getWorld().equals(player.getWorld()))
				continue;

			if (Methods.isRegionProtectedFromBuild(player, "EarthBlast",
					blast.location))
				continue;

			if (blast.player.equals(player))
				blast.redirect(player, getTargetLocation(player));

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= range
					&& Methods.getDistanceFromLine(vector, location,
							blast.location) < deflectrange
							&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				blast.redirect(player, getTargetLocation(player));
			}

		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (progressing) {
			if (location.distance(player.getLocation()) <= range) {
				// direction = Methods.getDirection(location, targetlocation)
				// .normalize();
				settingup = false;
				destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);

			if (blast.player.equals(player))
				continue;

			if (!blast.location.getWorld().equals(player.getWorld()))
				continue;

			if (!blast.progressing)
				continue;

			if (Methods.isRegionProtectedFromBuild(player, "EarthBlast",
					blast.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= range
					&& Methods.getDistanceFromLine(vector, location,
							blast.location) < deflectrange
							&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				blast.breakBlock();
			}

		}
	}

	public static String getDescription() {
		return "To use, place your cursor over an earthbendable object (dirt, rock, ores, etc) "
				+ "and tap sneak (default: shift). The object will temporarily turn to stone, "
				+ "indicating that you have it focused as the source for your ability. "
				+ "After you have selected an origin (you no longer need to be sneaking), "
				+ "simply left-click in any direction and you will see your object launch "
				+ "off in that direction, smashing into any creature in its path. If you look "
				+ "towards a creature when you use this ability, it will target that creature. "
				+ "A collision from Earth Blast both knocks the target back and deals some damage. "
				+ "You cannot have multiple of these abilities flying at the same time.";
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.location.getWorld().equals(location.getWorld()))
				if (blast.location.distance(location) <= radius)
					blast.breakBlock();
		}
	}
	public static ArrayList<EarthBlast> getAroundPoint(Location location, double radius) {
		ArrayList<EarthBlast> list = new ArrayList<EarthBlast>();
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.location.getWorld().equals(location.getWorld()))
				if (blast.location.distance(location) <= radius)
					list.add(blast);
		}
		return list;
	}	

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		boolean broke = false;
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.location.getWorld().equals(location.getWorld())
					&& !source.equals(blast.player))
				if (blast.location.distance(location) <= radius) {
					blast.breakBlock();
					broke = true;
				}
		}
		return broke;
	}

}