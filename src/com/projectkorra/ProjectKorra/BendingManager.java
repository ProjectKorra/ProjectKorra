package com.projectkorra.ProjectKorra;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	long time;
	long interval;

	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

    static FileConfiguration config;

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
		time = System.currentTimeMillis();
	}

	public void run() {
		try {
            config = plugin.getConfig();
			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			ProjectKorra.time_step = interval;

			AvatarState.manageAvatarStates();
			TempPotionEffect.progressAll();
			handleDayNight();
			Flight.handle();	
			RapidPunch.startPunchAll();
			RevertChecker.revertAirBlocks();
			handleCooldowns();
		} catch (Exception e) {
			Methods.stopBending();
			e.printStackTrace();
		}
	}

	public void handleCooldowns() {
		for (String bP: BendingPlayer.players.keySet()) {
			BendingPlayer bPlayer = BendingPlayer.players.get(bP);
			for (String abil: bPlayer.cooldowns.keySet()) {
				if (System.currentTimeMillis() >= bPlayer.cooldowns.get(abil)) {
					bPlayer.removeCooldown(abil);
				}
			}
		}
	}

	public void handleDayNight() {
		for (World world: Bukkit.getServer().getWorlds()) {
			if (!events.containsKey(world)) {
				events.put(world, "");
			}
		}
		for (World world: Bukkit.getServer().getWorlds()) {
			if (!times.containsKey(world)) {
				if (Methods.isDay(world)) {
					times.put(world, true);
				} else {
					times.put(world, false);
				}
			} else {
				if (times.get(world) && !Methods.isDay(world)) {
					// The hashmap says it is day, but it is not.
					times.put(world, false); // Sets time to night.
					if (Methods.hasRPG()) {
						if (RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.LunarEclipse.toString());
						}
						else if (Methods.isFullMoon(world)) {
							events.put(world, "FullMoon");
						}
						else {
							events.put(world, "");
						}
					} else {
						if (Methods.isFullMoon(world)) {
							events.put(world, "FullMoon");
						} else {
							events.put(world, "");
						}
					}
					for (Player player: world.getPlayers()) {
						if (Methods.isBender(player.getName(), Element.Water)) {
							if (Methods.hasRPG()) {
								if (RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(Methods.getWaterColor() + config.getString("messages.lunareclipse"));
								} else if (Methods.isFullMoon(world)) {
									player.sendMessage(Methods.getWaterColor() + config.getString("messages.fullmoonrise"));
								} else {
									player.sendMessage(Methods.getWaterColor() + config.getString("messages.moonrise"));
								}
							} else {
								if (Methods.isFullMoon(world)) {
									player.sendMessage(Methods.getWaterColor() + config.getString("messages.fullmoonrise"));
								} else {
									player.sendMessage(Methods.getWaterColor() + config.getString("messages.moonrise"));
								}
							}
						}
						if (Methods.isBender(player.getName(), Element.Fire)) {
							player.sendMessage(Methods.getFireColor() + config.getString("messages.sunset"));
						}
					}
				}

				if (!times.get(world) && Methods.isDay(world)) {
					// The hashmap says it is night, but it is day.
					times.put(world, true);
					if (Methods.hasRPG()) {
						if (RPGMethods.isSozinsComet(world)) {
							events.put(world, WorldEvents.SozinsComet.toString());
						}
						else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.SolarEclipse.toString());
						}
						else {
							events.put(world, "");
						}
					} else {
						events.put(world, "");
					}
					for (Player player: world.getPlayers()) {
						if (Methods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(Methods.getWaterColor() + config.getString("messages.moonset"));
						}
						if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							if (Methods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(Methods.getFireColor() + config.getString("messages.sozinscomet"));
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(Methods.getFireColor() + config.getString("messages.solareclipse"));
								} else {
									player.sendMessage(Methods.getFireColor() + config.getString("messages.sunrise"));
								}
							} else {
								player.sendMessage(Methods.getFireColor() + config.getString("messages.sunrise"));
							}
						}
					}
				}
			}
		}
		
	}
}
