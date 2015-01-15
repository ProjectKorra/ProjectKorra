package com.projectkorra.ProjectKorra;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	long time;
	long interval;

	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	static final String defaultsozinscometmessage = "Sozin's Comet is passing overhead! Firebending is now at its most powerful.";
	static final String defaultsolareclipsemessage = "A solar eclipse is out! Firebenders are temporarily powerless.";
	static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	static final String defaultfullmoonrisemessage = "A full moon is rising, empowering your waterbending like never before.";
	static final String defaultlunareclipsemessage = "A lunar eclipse is out! Waterbenders are temporarily powerless.";
	static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
		time = System.currentTimeMillis();
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
			ChiComboManager.handleParalysis();
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
						
						if(!player.hasPermission("bending.message.nightmessage")) return;
						
						if (Methods.isBender(player.getName(), Element.Water)) {
							if (Methods.hasRPG()) {
								if (RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(Methods.getWaterColor() + defaultlunareclipsemessage);
								} else if (Methods.isFullMoon(world)) {
									player.sendMessage(Methods.getWaterColor() + defaultfullmoonrisemessage);
								} else {
									player.sendMessage(Methods.getWaterColor() + defaultmoonrisemessage);
								}
							} else {
								if (Methods.isFullMoon(world)) {
									player.sendMessage(Methods.getWaterColor() + defaultfullmoonrisemessage);
								} else {
									player.sendMessage(Methods.getWaterColor() + defaultmoonrisemessage);
								}
							}
						}
						if (Methods.isBender(player.getName(), Element.Fire)) {
							if(player.hasPermission("bending.message.daymessage")) return;
							player.sendMessage(Methods.getFireColor() + defaultsunsetmessage);
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
							player.sendMessage(Methods.getWaterColor() + defaultmoonsetmessage);
						}
						if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							if (Methods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(Methods.getFireColor() + defaultsozinscometmessage);
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(Methods.getFireColor() + defaultsolareclipsemessage);
								} else {
									player.sendMessage(Methods.getFireColor() + defaultsunrisemessage);
								}
							} else {
								player.sendMessage(Methods.getFireColor() + defaultsunrisemessage);
							}
						}
					}
				}
			}
		}
		
	}
}
