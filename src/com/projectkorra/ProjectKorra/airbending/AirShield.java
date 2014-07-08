package com.projectkorra.ProjectKorra.airbending;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.firebending.FireBlast;

public class AirShield {

	public static ConcurrentHashMap<Integer, AirShield> instances = new ConcurrentHashMap<Integer, AirShield>();

	private static double maxradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.AirShield.Radius");
	private static int numberOfStreams = (int) (.75 * (double) maxradius);
	private static boolean isToggle = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.AirShield.IsAvatarStateToggle");

	private double radius = 2;

	private double speedfactor;

	private Player player;
	private HashMap<Integer, Integer> angles = new HashMap<Integer, Integer>();

	public AirShield(Player player) {
		if (AvatarState.isAvatarState(player)
				&& instances.containsKey(player.getEntityId()) && isToggle) {
			instances.remove(player.getEntityId());
			return;
		}
		this.player = player;
		int angle = 0;
		int di = (int) (maxradius * 2 / numberOfStreams);
		for (int i = -(int) maxradius + di; i < (int) maxradius; i += di) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360)
				angle = 0;
		}

		instances.put(player.getEntityId(), this);
	}

	private void rotateShield() {
		Location origin = player.getLocation();

		FireBlast.removeFireBlastsAroundPoint(origin, radius);

		for (Entity entity : Methods.getEntitiesAroundPoint(origin, radius)) {
			if (Methods.isRegionProtectedFromBuild(player, "AirShield",
					entity.getLocation()))
				continue;
			if (origin.distance(entity.getLocation()) > 2) {
				double x, z, vx, vz, mag;
				double angle = 50;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - origin.getX();
				z = entity.getLocation().getZ() - origin.getZ();

				mag = Math.sqrt(x * x + z * z);

				vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
				vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

				Vector velocity = entity.getVelocity();
				if (AvatarState.isAvatarState(player)) {
					velocity.setX(AvatarState.getValue(vx));
					velocity.setZ(AvatarState.getValue(vz));
				} else {
					velocity.setX(vx);
					velocity.setZ(vz);
				}

				velocity.multiply(radius / maxradius);
				entity.setVelocity(velocity);
				entity.setFallDistance(0);
			}
		}

		Set<Integer> keys = angles.keySet();
		for (int i : keys) {
			double x, y, z;
			double angle = (double) angles.get(i);
			angle = Math.toRadians(angle);

			double factor = radius / maxradius;

			y = origin.getY() + factor * (double) i;

			//double theta = Math.asin(y/radius);
			double f = Math.sqrt(1 - factor * factor * ((double) i / radius)
					* ((double) i / radius));

			x = origin.getX() + radius * Math.cos(angle) * f;
			z = origin.getZ() + radius * Math.sin(angle) * f;

			Location effect = new Location(origin.getWorld(), x, y, z);
			if (!Methods.isRegionProtectedFromBuild(player, "AirShield",
					effect))
				origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
						(int) AirBlast.defaultrange);

			angles.put(i, angles.get(i) + (int) (10 * speedfactor));
		}

		if (radius < maxradius) {
			radius += .3;
		}

		if (radius > maxradius)
			radius = maxradius;

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player.getEntityId());
			return false;
		}
		if (Methods.isRegionProtectedFromBuild(player, "AirShield",
				player.getLocation())) {
			instances.remove(player.getEntityId());
			return false;
		}
		speedfactor = 1;
		if (!Methods.canBend(player.getName(), "AirShield")
				|| player.getEyeLocation().getBlock().isLiquid()) {
			instances.remove(player.getEntityId());
			return false;
		}

		if (Methods.getBoundAbility(player) == null) {
			instances.remove(player.getEntityId());
			return false;
		}

		if (isToggle) {
			if (((!Methods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
					.isSneaking())) && !AvatarState.isAvatarState(player)) {
				instances.remove(player.getEntityId());
				return false;
			}
		} else {
			if (((!Methods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
					.isSneaking()))) {
				instances.remove(player.getEntityId());
				return false;
			}
		}

		//
		//		if (((!Methods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
		//				.isSneaking()))) {
		//			instances.remove(player.getEntityId());
		//			return false;
		//		}
		rotateShield();
		return true;
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static String getDescription() {
		return "Air Shield is one of the most powerful defensive techniques in existence. "
				+ "To use, simply sneak (default: shift). "
				+ "This will create a whirlwind of air around the user, "
				+ "with a small pocket of safe space in the center. "
				+ "This wind will deflect all projectiles and will prevent any creature from "
				+ "entering it for as long as its maintained. ";
	}
}