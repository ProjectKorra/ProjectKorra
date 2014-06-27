package com.projectkorra.ProjectKorra.firebending;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class ArcOfFire {

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	// static final long soonesttime = Tools.timeinterval;

	private static int defaultarc = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Blaze.ArcOfFire.Arc");
	private static int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Blaze.ArcOfFire.Range");
	private static int stepsize = 2;
	
	public static Map<String, Long> cooldowns = new HashMap<String, Long>();

	public ArcOfFire(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		// timers.put(player, System.currentTimeMillis());
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		Location location = player.getLocation();

		int arc = (int) Methods.firebendingDayAugment(defaultarc,
				player.getWorld());

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

			int range = defaultrange;
			if (AvatarState.isAvatarState(player))
				range = AvatarState.getValue(range);

			new FireStream(location, direction, player, range);
		}

		cooldowns.put(player.getName(), System.currentTimeMillis());
	}

	public static String getDescription() {
		return "To use, simply left-click in any direction. "
				+ "An arc of fire will flow from your location, "
				+ "igniting anything in its path."
				+ " Additionally, tap sneak to engulf the area around you "
				+ "in roaring flames.";
	}

}