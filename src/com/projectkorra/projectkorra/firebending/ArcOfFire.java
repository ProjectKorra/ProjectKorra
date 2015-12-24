package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Used for the ability Blaze.
 */
public class ArcOfFire implements ConfigLoadable {

	private static int defaultarc = config.get().getInt("Abilities.Fire.Blaze.ArcOfFire.Arc");
	private static int defaultrange = config.get().getInt("Abilities.Fire.Blaze.ArcOfFire.Range");
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Blaze.Cooldown");
	private static int stepsize = 2;

	public ArcOfFire(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Blaze"))
			return;
		/* End Initial Checks */
		// reloadVariables();

		Location location = player.getLocation();

		int arc = (int) FireMethods.getFirebendingDayAugment(defaultarc, player.getWorld());

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

		bPlayer.addCooldown("Blaze", cooldown);
	}

	public static String getDescription() {
		return "To use, simply left-click in any direction. " + "An arc of fire will flow from your location, "
				+ "igniting anything in its path." + " Additionally, tap sneak to engulf the area around you "
				+ "in roaring flames.";
	}

	@Override
	public void reloadVariables() {
		defaultarc = config.get().getInt("Abilities.Fire.Blaze.ArcOfFire.Arc");
		defaultrange = config.get().getInt("Abilities.Fire.Blaze.ArcOfFire.Range");
	}

}
