package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Tornado {
	
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Integer, Tornado> instances = new ConcurrentHashMap<Integer, Tornado>();

	private static double maxheight = config.getDouble("Abilities.Air.Tornado.Height");
	private static double PCpushfactor = config.getDouble("Abilities.Air.Tornado.PlayerPushFactor");
	private static double maxradius = config.getDouble("Abilities.Air.Tornado.Radius");
	private static double range = config.getDouble("Abilities.Air.Tornado.Range");
	private static double NPCpushfactor = config.getDouble("Abilities.Air.Tornado.MobPushFactor");
	private static int numberOfStreams = (int) (.3 * (double) maxheight);
	// private static double speed = .75;

	private double height = 2;
	private double radius = height / maxheight * maxradius;

	// private static double speedfactor = 1000 * speed
	// * (Bending.time_step / 1000.);
	private static double speedfactor = 1;

	private ConcurrentHashMap<Integer, Integer> angles = new ConcurrentHashMap<Integer, Integer>();
	private Location origin;
	private Player player;

	// private boolean canfly;

	public Tornado(Player player) {
		this.player = player;
		// canfly = player.getAllowFlight();
		// player.setAllowFlight(true);
		origin = player.getTargetBlock(null, (int) range).getLocation();
		origin.setY(origin.getY() - 1. / 10. * height);

		int angle = 0;
		for (int i = 0; i <= maxheight; i += (int) maxheight / numberOfStreams) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360)
				angle = 0;
		}

		new Flight(player);
		player.setAllowFlight(true);
		instances.put(player.getEntityId(), this);

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			// player.setAllowFlight(canfly);
			instances.remove(player.getEntityId());
			return false;
		}
		if (!Methods.canBend(player.getName(), "Tornado") || player.getEyeLocation().getBlock().isLiquid()) {
			// player.setAllowFlight(canfly);
			instances.remove(player.getEntityId());
			return false;
		}
		String abil = Methods.getBoundAbility(player);
		if (abil == null) {
			instances.remove(player.getEntityId());
			return false;
		}
		if (!abil.equalsIgnoreCase("Tornado") || !player.isSneaking()) {
			instances.remove(player.getEntityId());
			return false;
		}

		if (Methods.isRegionProtectedFromBuild(player, "AirBlast", origin)) {
			instances.remove(player.getEntityId());
			return false;
		}
		rotateTornado();
		return true;
	}

	private void rotateTornado() {
		origin = player.getTargetBlock(null, (int) range).getLocation();

		double timefactor = height / maxheight;
		radius = timefactor * maxradius;

		if (origin.getBlock().getType() != Material.AIR) {
			origin.setY(origin.getY() - 1. / 10. * height);

			for (Entity entity : Methods.getEntitiesAroundPoint(origin, height)) {
				if (Methods.isRegionProtectedFromBuild(player, "AirBlast", entity.getLocation()))
					continue;
				double y = entity.getLocation().getY();
				double factor;
				if (y > origin.getY() && y < origin.getY() + height) {
					factor = (y - origin.getY()) / height;
					Location testloc = new Location(origin.getWorld(), origin.getX(), y, origin.getZ());
					if (testloc.distance(entity.getLocation()) < radius	* factor) {
						double x, z, vx, vz, mag;
						double angle = 100;
						double vy = 0.7 * NPCpushfactor;
						angle = Math.toRadians(angle);

						x = entity.getLocation().getX() - origin.getX();
						z = entity.getLocation().getZ() - origin.getZ();

						mag = Math.sqrt(x * x + z * z);

						vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
						vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

						if (entity instanceof Player) {
							vy = 0.05 * PCpushfactor;
						}

						if (entity.getEntityId() == player.getEntityId()) {
							Vector direction = player.getEyeLocation().getDirection().clone().normalize();
							vx = direction.getX();
							vz = direction.getZ();
							Location playerloc = player.getLocation();
							double py = playerloc.getY();
							double oy = origin.getY();
							double dy = py - oy;
							if (dy >= height * .95) {
								vy = 0;
							} else if (dy >= height * .85) {
								vy = 6.0 * (.95 - dy / height);
							} else {
								vy = .6;
							}
						}

						Vector velocity = entity.getVelocity();
						velocity.setX(vx);
						velocity.setZ(vz);
						velocity.setY(vy);
						velocity.multiply(timefactor);
						entity.setVelocity(velocity);
						entity.setFallDistance(0);

						if (entity instanceof Player) {
							new Flight((Player) entity);
						}
					}
				}
			}

			for (int i : angles.keySet()) {
				double x, y, z;
				double angle = (double) angles.get(i);
				angle = Math.toRadians(angle);
				double factor;

				y = origin.getY() + timefactor * (double) i;
				factor = (double) i / height;

				x = origin.getX() + timefactor * factor * radius * Math.cos(angle);
				z = origin.getZ() + timefactor * factor * radius * Math.sin(angle);

				Location effect = new Location(origin.getWorld(), x, y, z);
				if (!Methods.isRegionProtectedFromBuild(player, "AirBlast", effect))
					Methods.playAirbendingParticles(effect, 20);
//					origin.getWorld().playEffect(effect, Effect.SMOKE, 4, (int) AirBlast.defaultrange);

				angles.put(i, angles.get(i) + 25 * (int) speedfactor);
			}
		}

		if (height < maxheight) {
			height += 1;
		}

		if (height > maxheight) {
			height = maxheight;
		}

	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (int id : instances.keySet()) {
			players.add(instances.get(id).player);
		}
		return players;
	}

}