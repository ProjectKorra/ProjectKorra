package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class AirSwipe {

	public static ConcurrentHashMap<Integer, AirSwipe> instances = new ConcurrentHashMap<Integer, AirSwipe>();
	public static Map<String, Long> cooldowns = new HashMap<String, Long>();
	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	// static final long soonesttime = ConfigManager.airSwipeCooldown;

	public static FileConfiguration config = ProjectKorra.plugin.getConfig();
	
	private static int ID = Integer.MIN_VALUE;

	private static int defaultdamage = config.getInt("Abilities.Air.AirSwipe.Damage");
	private static double affectingradius = config.getDouble("Abilities.Air.AirSwipe.Radius");
	private static double defaultpushfactor = config.getDouble("Abilities.Air.AirSwipe.Push");
	private static double range = config.getDouble("Abilities.Air.AirSwipe.Range");
	private static int arc = config.getInt("Abilities.Air.AirSwipe.Arc");
	private static int stepsize = 4;
	private static double speed = config.getDouble("Abilities.Air.AirSwipe.Speed");
	private static byte full = AirBlast.full;
	private static long maxchargetime = 3000;
	private static double maxfactor = 3;

	private double speedfactor;

	private static Integer[] breakables = { 6, 31, 32, 37, 38, 39, 40, 59, 81,
		83, 106 };

	private Location origin;
	private Player player;
	private boolean charging = false;
	private long time;
	private int damage = defaultdamage;
	private double pushfactor = defaultpushfactor;
	private int id;
	private ConcurrentHashMap<Vector, Location> elements = new ConcurrentHashMap<Vector, Location>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirSwipe(Player player) {
		this(player, false);
	}

	public AirSwipe(Player player, boolean charging) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + config.getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		this.player = player;
		this.charging = charging;
		origin = player.getEyeLocation();
		time = System.currentTimeMillis();

		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		id = ID++;

		instances.put(id, this);

		cooldowns.put(player.getName(), System.currentTimeMillis());

		if (!charging)
			launch();

		// timers.put(player, System.currentTimeMillis());
	}

	private void launch() {
		origin = player.getEyeLocation();
		for (int i = -arc; i <= arc; i += stepsize) {
			double angle = Math.toRadians((double) i);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			elements.put(direction, origin);
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(id);
			return false;
		}
		speedfactor = speed * (ProjectKorra.time_step / 1000.);
		if (!charging) {
			if (elements.isEmpty()) {
				instances.remove(id);
				return false;
			}

			advanceSwipe();
		} else {
			if (Methods.getBoundAbility(player) == null) {
				instances.remove(id);
				return false;
			}
			if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirSwipe") || !Methods.canBend(player.getName(), "AirSwipe")) {
				instances.remove(id);
				return false;
			}

			if (!player.isSneaking()) {
				double factor = 1;
				if (System.currentTimeMillis() >= time + maxchargetime) {
					factor = maxfactor;
				} else {
					factor = maxfactor
							* (double) (System.currentTimeMillis() - time)
							/ (double) maxchargetime;
				}
				charging = false;
				launch();
				if (factor < 1)
					factor = 1;
				damage *= factor;
				pushfactor *= factor;
				return true;
			} else if (System.currentTimeMillis() >= time + maxchargetime) {
				player.getWorld().playEffect(
						player.getEyeLocation(),
						Effect.SMOKE,
						Methods.getIntCardinalDirection(player.getEyeLocation()
								.getDirection()), 3);
			}
		}
		return true;
	}

	private void advanceSwipe() {
		affectedentities.clear();
		for (Vector direction : elements.keySet()) {
			Location location = elements.get(direction);
			if (direction != null && location != null) {
				location = location.clone().add(
						direction.clone().multiply(speedfactor));
				elements.replace(direction, location);

				if (location.distance(origin) > range) {
					elements.remove(direction);
				} else {
					Methods.removeSpouts(location, player);

					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.annihilateBlasts(location,
									radius, source)
									|| FireBlast.annihilateBlasts(location, radius,
											source)) {
						elements.remove(direction);
						damage = 0;
						continue;
					}

					Block block = location.getBlock();
					for (Block testblock : Methods.getBlocksAroundPoint(location,
							affectingradius)) {
						if (testblock.getType() == Material.FIRE) {
							testblock.setType(Material.AIR);
						}
						if (isBlockBreakable(testblock)) {
							Methods.breakBlock(testblock);
						}
					}

					if (block.getType() != Material.AIR) {
						if (isBlockBreakable(block)) {
							Methods.breakBlock(block);
						} else {
							elements.remove(direction);
						}
						if (block.getType() == Material.LAVA
								|| block.getType() == Material.STATIONARY_LAVA) {
							if (block.getData() == full) {
								block.setType(Material.OBSIDIAN);
							} else {
								block.setType(Material.COBBLESTONE);
							}
						}
					} else {
						location.getWorld().playEffect(location, Effect.SMOKE,
								4, (int) AirBlast.defaultrange);
						affectPeople(location, direction);
					}
				}
				// } else {
				// elements.remove(direction);
			}

		}

		if (elements.isEmpty()) {
			instances.remove(id);
		}
	}

	private void affectPeople(Location location, Vector direction) {
		Methods.removeSpouts(location, player);
		for (Entity entity : Methods.getEntitiesAroundPoint(location,
				affectingradius)) {
//			if (Methods.isRegionProtectedFromBuild(player, Abilities.AirSwipe,
//					entity.getLocation()))
//				continue;
			if (entity.getEntityId() != player.getEntityId()) {
				if (AvatarState.isAvatarState(player)) {
					entity.setVelocity(direction.multiply(AvatarState
							.getValue(pushfactor)));
				} else {
					entity.setVelocity(direction.multiply(pushfactor));
				}

				if (entity instanceof LivingEntity
						&& !affectedentities.contains(entity)) {
					if (damage != 0)
						Methods.damageEntity(player, entity, damage);
					affectedentities.add(entity);
				}

				if (entity instanceof Player) {
					new Flight((Player) entity, player);
				}

				if (elements.containsKey(direction)) {
					elements.remove(direction);
				}
			}
		}
	}

	private boolean isBlockBreakable(Block block) {
		Integer id = block.getTypeId();
		if (Arrays.asList(breakables).contains(id)
				&& !Illumination.blocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static String getDescription() {
		return "To use, simply left-click in a direction. "
				+ "An arc of air will flow from you towards that direction, "
				+ "cutting and pushing back anything in its path. "
				+ "Its damage is minimal, but it still sends the message. "
				+ "This ability will extinguish fires, cool lava, and cut things like grass, "
				+ "mushrooms and flowers. Additionally, you can charge it by holding sneak. "
				+ "Charging before attacking will increase damage and knockback, up to a maximum.";
	}

	public static void charge(Player player) {
		new AirSwipe(player, true);
	}

}