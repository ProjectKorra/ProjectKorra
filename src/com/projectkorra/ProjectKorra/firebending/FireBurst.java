package com.projectkorra.ProjectKorra.firebending;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class FireBurst {
	private static ConcurrentHashMap<Player, FireBurst> instances = new ConcurrentHashMap<Player, FireBurst>();
	private static Map<String, Long> cooldowns = new HashMap<String, Long>();

	private Player player;
	private long starttime;
	private int damage = 3;
	private long chargetime = 2500;
	private double deltheta = 10;
	private double delphi = 10;
	private boolean charged = false;

	public FireBurst(Player player) {
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
		if (Methods.isDay(player.getWorld())) {
			chargetime /= ProjectKorra.plugin.getConfig().getDouble("Properties.Fire.DayFactor");
		}
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}

	public static void coneBurst(Player player) {
		if (instances.containsKey(player))
			instances.get(player).coneBurst();
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = Methods.getBlocksAroundPoint(
					player.getLocation(), 2);
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
						new FireBlast(location, direction.normalize(), player, damage, safeblocks);
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
			List<Block> safeblocks = Methods.getBlocksAroundPoint(player.getLocation(), 2);
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
					new FireBlast(location, direction.normalize(), player, damage, safeblocks);
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		instances.remove(player);
	}

	private void progress() {
		if (!Methods.canBend(player.getName(), "FireBurst")) {
			instances.remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			instances.remove(player);
			return;
		}
		
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("FireBurst")) {
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
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();
	}

	public static String getDescription() {
		return "FireBurst is a very powerful firebending ability. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of flames in front of you, or click to release the burst in a sphere around you. ";
	}

	public static void removeAll() {
		instances.clear();

	}
}