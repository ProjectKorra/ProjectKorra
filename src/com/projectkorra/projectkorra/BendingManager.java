package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BendingManager implements Runnable {

	private static BendingManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

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
				if (FireAbility.isDay(world)) {
					times.put(world, true);
				} else {
					times.put(world, false);
				}
			} else {
				if (times.get(world) && !FireAbility.isDay(world)) {
					// The hashmap says it is day, but it is not.
					times.put(world, false); // Sets time to night.
					for (Player player : world.getPlayers()) {
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (bPlayer == null) {
							continue;
						}
						
						if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.daymessage")) {
							player.sendMessage(Element.WATER.getColor() + getMoonriseMessage());
						}
						if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(Element.FIRE.getColor() + getSunsetMessage());
						}
					}
				}

				if (!times.get(world) && FireAbility.isDay(world)) {
					// The hashmap says it is night, but it is day.
					times.put(world, true);
					for (Player player : world.getPlayers()) {
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (bPlayer == null) {
							continue;
						}
						
						if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(Element.WATER.getColor() + getMoonsetMessage());
						}
						if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.daymessage")) {
							player.sendMessage(Element.FIRE.getColor() + getSunriseMessage());
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

			CoreAbility.progressAll();
			TempPotionEffect.progressAll();
			handleDayNight();
			Flight.handle();
			RevertChecker.revertAirBlocks();
			ChiCombo.handleParalysis();
			HorizontalVelocityTracker.updateAll();
			handleCooldowns();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSunriseMessage() {
		return getConfig().getString("Properties.Fire.DayMessage");
	}

	public static String getSunsetMessage() {
		return getConfig().getString("Properties.Fire.NightMessage");
	}

	public static String getMoonriseMessage() {
		return getConfig().getString("Properties.Water.NightMessage");
	}

	public static String getMoonsetMessage() {
		return getConfig().getString("Properties.Water.DayMessage");
	}

	private static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}
	
}
