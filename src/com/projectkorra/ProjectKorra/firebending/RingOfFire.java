package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class RingOfFire {

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	// static final long soonesttime = Tools.timeinterval;

	static final int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Blaze.RingOfFire.Range");

	public RingOfFire(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		// timers.put(player, System.currentTimeMillis());
		if (ArcOfFire.cooldowns.containsKey(player.getName())) {
			if (ArcOfFire.cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				ArcOfFire.cooldowns.remove(player.getName());
			}
		}

		Location location = player.getLocation();

		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
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

		ArcOfFire.cooldowns.put(player.getName(), System.currentTimeMillis());
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "A circle of fire will emanate from you, "
				+ "engulfing everything around you. Use with extreme caution.";
	}

}