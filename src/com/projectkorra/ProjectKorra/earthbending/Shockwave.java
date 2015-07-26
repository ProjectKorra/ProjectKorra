package com.projectkorra.ProjectKorra.earthbending;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class Shockwave {

    public static ConcurrentHashMap<Player, Shockwave> instances = new ConcurrentHashMap<>();

	private static final double angle = Math.toRadians(40);
	private static final long defaultchargetime = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Shockwave.ChargeTime");
	private static final double threshold = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Shockwave.FallThreshold");

	private Player player;
	private long starttime;
	private long chargetime = defaultchargetime;
	private boolean charged = false;

	public Shockwave(Player player) {
		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);

	}

	public static void fallShockwave(Player player) {
		if (!GeneralMethods.canBend(player.getName(), "Shockwave")) {
			return;
		}
        if (GeneralMethods.getBoundAbility(player) == null
                || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Shockwave")) {
            return;
		}

        if (instances.containsKey(player) || player.getFallDistance() < threshold
                || !EarthMethods.isEarthbendable(player, player.getLocation().add(0, -1, 0).getBlock())) {
            return;
		}

		areaShockwave(player);
	}

	private void progress() {
		if (GeneralMethods.getBoundAbility(player) == null) {
			instances.remove(player);
			return;
		}
        if (!GeneralMethods.canBend(player.getName(), "Shockwave")
                || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Shockwave")) {
            instances.remove(player);
			return;
		}

		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				areaShockwave(player);
				instances.remove(player);
			} else {
				instances.remove(player);
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					GeneralMethods.getIntCardinalDirection(player.getEyeLocation()
							.getDirection()), 3);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();
		Ripple.progressAll();
	}

	private static void areaShockwave(Player player) {
		double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			double rtheta = Math.toRadians(theta);
			Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			new Ripple(player, vector.normalize());
		}
	}

	public static void coneShockwave(Player player) {
		if (instances.containsKey(player)) {
			if (instances.get(player).charged) {
				double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0,
							Math.sin(rtheta));
					if (vector.angle(player.getEyeLocation().getDirection()) < angle)
						new Ripple(player, vector.normalize());
				}
				instances.remove(player);
			}
		}
	}

	public static String getDescription() {
		return "This is one of the most powerful moves in the earthbender's arsenal. "
				+ "To use, you must first charge it by holding sneak (default: shift). "
				+ "Once charged, you can release sneak to create an enormous shockwave of earth, "
				+ "disturbing all earth around you and expanding radially outwards. "
				+ "Anything caught in the shockwave will be blasted back and dealt damage. "
				+ "If you instead click while charged, the disruption is focused in a cone in front of you. "
				+ "Lastly, if you fall from a great enough height with this ability selected, you will automatically create a shockwave.";
	}

	public static void removeAll() {
		instances.clear();
		Ripple.removeAll();

	}

	public Player getPlayer() {
		return player;
	}

	public long getChargetime() {
		return chargetime;
	}

	public void setChargetime(long chargetime) {
		this.chargetime = chargetime;
	}

}