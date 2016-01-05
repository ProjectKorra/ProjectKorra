package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.chiblocking.RapidPunch;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BendingManager implements Runnable, ConfigLoadable {

	private static BendingManager instance;

	private static String sunriseMessage = config.get().getString("Properties.Fire.DayMessage");
	private static String sunsetMessage = config.get().getString("Properties.Fire.NightMessage");

	private static String moonriseMessage = config.get().getString("Properties.Water.NightMessage");
	private static String moonsetMessage = config.get().getString("Properties.Water.DayMessage");

	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time

	public BendingManager() {
		instance = this;
		time = System.currentTimeMillis();
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (UUID uuid : BendingPlayer.getPlayers().keySet()) {
			BendingPlayer bPlayer = BendingPlayer.getPlayers().get(uuid);
			for (String abil : bPlayer.getCooldowns().keySet()) {
				if (System.currentTimeMillis() >= bPlayer.getCooldown(abil)) {
					bPlayer.removeCooldown(abil);
				}
			}
		}
	}

	public void handleDayNight() {
		for (World world : Bukkit.getServer().getWorlds()) {
			if (!times.containsKey(world)) {
				if (FireMethods.isDay(world)) {
					times.put(world, true);
				} else {
					times.put(world, false);
				}
			} else {
				if (times.get(world) && !FireMethods.isDay(world)) {
					// The hashmap says it is day, but it is not.
					times.put(world, false); // Sets time to night.
					for (Player player : world.getPlayers()) {
						if (GeneralMethods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.daymessage")) {
							player.sendMessage(WaterMethods.getWaterColor() + moonriseMessage);
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(FireMethods.getFireColor() + sunsetMessage);
						}
					}
				}

				if (!times.get(world) && FireMethods.isDay(world)) {
					// The hashmap says it is night, but it is day.
					times.put(world, true);
					for (Player player : world.getPlayers()) {
						if (GeneralMethods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(WaterMethods.getWaterColor() + moonsetMessage);
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							player.sendMessage(FireMethods.getFireColor() + sunriseMessage);
						}
					}
				}
			}
		}
	}

	public void run() {
		try {
			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			ProjectKorra.time_step = interval;

			AvatarState.manageAvatarStates();
			TempPotionEffect.progressAll();
			handleDayNight();
			Flight.handle();
			RapidPunch.startPunchAll();
			RevertChecker.revertAirBlocks();
			ChiCombo.handleParalysis();
			HorizontalVelocityTracker.updateAll();
			handleCooldowns();
		}
		catch (Exception e) {
			//GeneralMethods.stopBending();
			e.printStackTrace();
		}
	}

	@Override
	public void reloadVariables() {
		sunriseMessage = config.get().getString("Properties.Fire.DayMessage");
		sunsetMessage = config.get().getString("Properties.Fire.NightMessage");

		moonriseMessage = config.get().getString("Properties.Water.NightMessage");
		moonsetMessage = config.get().getString("Properties.Water.DayMessage");
	}

}
