package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EarthBlast {

	public static ConcurrentHashMap<Integer, EarthBlast> instances = new ConcurrentHashMap<Integer, EarthBlast>();

	private static boolean hitself = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthBlast.CanHitSelf");
	private static double preparerange = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.PrepareRange");
	private static double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Range");
	private static double DAMAGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Damage");
	private static double speed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Speed");
	private static final double deflectrange = 3;

	private static boolean revert = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthBlast.Revert");
	private static double PUSH_FACTOR = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthBlast.Push");

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
	private double range = RANGE;
	private double damage = DAMAGE;
	private double pushfactor = PUSH_FACTOR;

	public EarthBlast(Player player) {
		this.player = player;
		if (prepare()) {
			id = ID++;
			if (ID >= Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			instances.put(id, this);
			time = System.currentTimeMillis();
		}

	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockSource.getEarthSourceBlock(player, range, ClickType.SHIFT_DOWN);
		block(player);
		if (block != null) {
			if (block.getLocation().distance(player.getLocation()) > preparerange) {
				return false;
			}
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private static Location getTargetLocation(Player player) {
		Entity target = GeneralMethods.getTargetedEntity(player, RANGE, new ArrayList<Entity>());
		Location location;
		if (target == null) {
			location = GeneralMethods.getTargetedLocation(player, RANGE);
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
	}

	private void cancelPrevious() {

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
			sourcetype = Material.SAND;
			if (sourceblock.getData() == (byte) 0x1) {
				sourceblock.setType(Material.RED_SANDSTONE);
			} else {
				sourceblock.setType(Material.SANDSTONE);
			}
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
		if(destination != null){
			breakBlock();
			return;
		}
		if (sourceblock.getType() == Material.SAND) {
			if (sourceblock.getData() == (byte) 0x1) {
				sourceblock.setType(sourcetype);
				sourceblock.setData((byte) 0x1);
			}
			else{
				sourceblock.setType(sourcetype);
			}
		}
		else {
			sourceblock.setType(sourcetype);
		}
		instances.remove(id);
	}

	@SuppressWarnings("deprecation")
	public void throwEarth() {
		if (sourceblock != null) {
			if (sourceblock.getWorld().equals(player.getWorld())) {
				if (EarthMethods.movedearth.containsKey(sourceblock)) {
					if (!revert)
						EarthMethods.removeRevertIndex(sourceblock);
				}
				Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
				// Methods.verbose(target);
				if (target == null) {
					destination = player.getTargetBlock(
							EarthMethods.getTransparentEarthbending(), (int) range)
							.getLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
				} else {
					destination = ((LivingEntity) target).getEyeLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
					destination = GeneralMethods.getPointOnLine(firstdestination,
							destination, range);
				}
				if (destination.distance(location) <= 1) {
					progressing = false;
					destination = null;
				} else {
					progressing = true;
					EarthMethods.playEarthbendingSound(sourceblock.getLocation());
					// direction = getDirection().normalize();
					if (sourcetype != Material.SAND && sourcetype != Material.GRAVEL) {
						sourceblock.setType(sourcetype);
					}
				}
			}

		}
	}

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
				|| !GeneralMethods.canBend(player.getName(), "EarthBlast")) {
			breakBlock();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (falling) {
				breakBlock();
				return false;
			}

			if (!EarthMethods.isEarthbendable(player, sourceblock)
					&& sourceblock.getType() != Material.COBBLESTONE) {
				instances.remove(id);
				return false;
			}

			if (!progressing && !falling) {

				if (GeneralMethods.getBoundAbility(player) == null) {
					unfocusBlock();
					return false;
				}

				if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("EarthBlast")) {
					unfocusBlock();
					return false;
				}

				if (sourceblock == null) {
					instances.remove(id);
					return false;
				}
				if (!player.getWorld().equals(sourceblock.getWorld())) {
					unfocusBlock();
					return false;
				}
				if (sourceblock.getLocation().distance(player.getLocation()) > preparerange) {
					unfocusBlock();
					return false;
				}
			}

			if (falling) {
				breakBlock();

			} else {
				if (!progressing) {
					return false;
				}

				if (sourceblock.getY() == firstdestination.getBlockY())
					settingup = false;

				Vector direction;
				if (settingup) {
					direction = GeneralMethods.getDirection(location, firstdestination)
							.normalize();
				} else {
					direction = GeneralMethods.getDirection(location, destination)
							.normalize();
				}

				location = location.clone().add(direction);

				WaterMethods.removeWaterSpouts(location, player);
				AirMethods.removeAirSpouts(location, player);

				Block block = location.getBlock();
				if (block.getLocation().equals(sourceblock.getLocation())) {
					location = location.clone().add(direction);
					block = location.getBlock();
				}

				if (EarthMethods.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					GeneralMethods.breakBlock(block);
				} else if (!settingup) {
					breakBlock();
					return false;
				} else {
					location = location.clone().subtract(direction);
					direction = GeneralMethods.getDirection(location, destination)
							.normalize();
					location = location.clone().add(direction);

					WaterMethods.removeWaterSpouts(location, player);
					AirMethods.removeAirSpouts(location, player);
					double radius = FireBlast.AFFECTING_RADIUS;
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

					if (EarthMethods.isTransparentToEarthbending(player, block)
							&& !block.isLiquid()) {
						GeneralMethods.breakBlock(block);
					} else {
						breakBlock();
						return false;
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location,
						FireBlast.AFFECTING_RADIUS)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player,
							"EarthBlast", entity.getLocation()))
						continue;
					if (entity instanceof LivingEntity
							&& (entity.getEntityId() != player.getEntityId() || hitself)) {

						AirMethods.breakBreathbendingHold(entity);

						Location location = player.getEyeLocation();
						Vector vector = location.getDirection();
						entity.setVelocity(vector.normalize().multiply(pushfactor));
                                                double damage = this.damage;
						if (EarthMethods.isMetal(sourceblock) && EarthMethods.canMetalbend(player)) {
							damage = EarthMethods.getMetalAugment(this.damage);
						}
						GeneralMethods.damageEntity(player, entity, damage);
						progressing = false;
					}
				}

				if (!progressing) {
					breakBlock();
					return false;
				}

				if (revert) {
					// Methods.addTempEarthBlock(sourceblock, block);
					if (sourceblock.getType() == Material.RED_SANDSTONE) {
						sourceblock.setType(sourcetype);
						if(sourcetype == Material.SAND)
							sourceblock.setData((byte) 0x1);
					}
					else {
						sourceblock.setType(sourcetype);
					}
					EarthMethods.moveEarthBlock(sourceblock, block);
					
					if (block.getType() == Material.SAND)
						block.setType(Material.SANDSTONE);
					if (block.getType() == Material.GRAVEL)
						block.setType(Material.STONE);
				} else {
					block.setType(sourceblock.getType());
					sourceblock.setType(Material.AIR);
				}

				sourceblock = block;

				if (location.distance(destination) < 1) {
					if (sourcetype == Material.SAND || sourcetype == Material.GRAVEL) {
						progressing = false;
						if (sourceblock.getType() == Material.RED_SANDSTONE) {
							sourcetype = Material.SAND;
							sourceblock.setType(sourcetype);
							sourceblock.setData((byte) 0x1);
						}
						else {
							sourceblock.setType(sourcetype);
						}
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
			EarthMethods.addTempAirBlock(sourceblock);
		} else {
			sourceblock.breakNaturally();
		}

		instances.remove(id);
	}

	public static void throwEarth(Player player) {
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();

		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
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
			bPlayer.addCooldown("EarthBlast", GeneralMethods.getGlobalCooldown());

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

			if (GeneralMethods.isRegionProtectedFromBuild(player, "EarthBlast",
					blast.location))
				continue;

			if (blast.player.equals(player))
				blast.redirect(player, getTargetLocation(player));

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= RANGE
					&& GeneralMethods.getDistanceFromLine(vector, location,
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

			if (GeneralMethods.isRegionProtectedFromBuild(player, "EarthBlast",
					blast.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= RANGE
					&& GeneralMethods.getDistanceFromLine(vector, location,
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

	public Player getPlayer() {
		return player;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}

}