package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class AirBurst {

	private static ConcurrentHashMap<Player, AirBurst> instances = new ConcurrentHashMap<Player, AirBurst>();
	private static ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
	
	private static double threshold = 10;
	private static double pushfactor = 1.5;
	private static double deltheta = 10;
	private static double delphi = 10;

	private Player player;
	private long starttime;
	private long chargetime = 1750;
	private boolean charged = false;

	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirBurst(Player player) {
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}

	public AirBurst() {

	}

	public static void coneBurst(Player player) {
		if (instances.containsKey(player))
			instances.get(player).coneBurst();
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						// Methods.verbose(direction.angle(vector));
						// Methods.verbose(direction);
						new AirBlast(location, direction.normalize(), player,
								pushfactor, this);
					}
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		instances.remove(player);
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					new AirBlast(location, direction.normalize(), player,
							pushfactor, this);
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		instances.remove(player);
	}

	public static void fallBurst(Player player) {
		if (!Methods.canBend(player.getName(), "AirBurst")) {
			return;
		}
		if (player.getFallDistance() < threshold) {
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			return;
		}
		if (instances.containsKey(player)) {
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
			return;
		}

		Location location = player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += deltheta) {
			double dphi = delphi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				new AirBlast(location, direction.normalize(), player,
						pushfactor, new AirBurst());
			}
		}
	}

	void addAffectedEntity(Entity entity) {
		affectedentities.add(entity);
	}

	boolean isAffectedEntity(Entity entity) {
		return affectedentities.contains(entity);
	}

	private void progress() {
		if (!Methods.canBend(player.getName(), "AirBurst")) {
			instances.remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			instances.remove(player);
			return;
		}
		
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
			instances.remove(player);
			return;
		}

		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				sphereBurst();
			} else {
				instances.remove(player);
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			Methods.playAirbendingParticles(location);
//			location.getWorld().playEffect(
//					location,
//					Effect.SMOKE,
//					Methods.getIntCardinalDirection(player.getEyeLocation()
//							.getDirection()), 3);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();
	}

	public static void removeAll() {
		instances.clear();

	}
}