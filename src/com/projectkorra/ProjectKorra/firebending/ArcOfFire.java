package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class ArcOfFire {

	private static int defaultarc = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Blaze.ArcOfFire.Arc");
	private static int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Blaze.ArcOfFire.Range");
	private static int stepsize = 2;
	
	public ArcOfFire(Player player) {
		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		
		if (bPlayer.isOnCooldown("Blaze")) return;
		Location location = player.getLocation();

		int arc = (int) Methods.getFirebendingDayAugment(defaultarc,
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
			else
				range = (int) Methods.getFirebendingDayAugment(range, player.getWorld());

			new FireStream(location, direction, player, range);
		}

		bPlayer.addCooldown("Blaze", Methods.getGlobalCooldown());
	}

	public static String getDescription() {
		return "To use, simply left-click in any direction. "
				+ "An arc of fire will flow from your location, "
				+ "igniting anything in its path."
				+ " Additionally, tap sneak to engulf the area around you "
				+ "in roaring flames.";
	}

}