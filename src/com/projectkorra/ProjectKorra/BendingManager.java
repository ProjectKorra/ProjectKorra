package com.projectkorra.ProjectKorra;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Objects.HorizontalVelocityTracker;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.firebending.FireMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;
	
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	static final String DEFAULT_SOZINS_COMET_MESSAGE = "Sozin's Comet is passing overhead! Firebending is now at its most powerful.";
	static final String DEFAULT_SOLAR_ECLIPSE_MESSAGE = "A solar eclipse is out! Firebenders are temporarily powerless.";

	static final String DEFAULT_SUNRISE_MESSAGE = "You feel the strength of the rising sun empowering your firebending.";
	static final String DEFAULT_SUNSET_MESSAGE = "You feel the empowering of your firebending subside as the sun sets.";

	static final String DEFAULT_MOONRISE_MESSAGE = "You feel the strength of the rising moon empowering your waterbending.";
	static final String DEFAULT_FULL_MOONRISE_MESSAGE = "A full moon is rising, empowering your waterbending like never before.";
	static final String DEFAULT_LUNAR_ECLIPSE_MESSAGE = "A lunar eclipse is out! Waterbenders are temporarily powerless.";
	static final String DEFAULT_MOONSET_MESSAGE = "You feel the empowering of your waterbending subside as the moon sets.";
	
	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
		time = System.currentTimeMillis();
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
				if (FireMethods.isDay(world)) {
					times.put(world, true);
				} else {
					times.put(world, false);
				}
			} else {
				if (times.get(world) && !FireMethods.isDay(world)) {
					// The hashmap says it is day, but it is not.
					times.put(world, false); // Sets time to night.
					if (GeneralMethods.hasRPG()) {
						if (RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.LunarEclipse.toString());
						} else if (WaterMethods.isFullMoon(world)) {
							events.put(world, "FullMoon");
						} else {
							events.put(world, "");
						}
					} else {
						if (WaterMethods.isFullMoon(world)) {
							events.put(world, "FullMoon");
						} else {
							events.put(world, "");
						}
					}
					for (Player player: world.getPlayers()) {
						
						if(!player.hasPermission("bending.message.nightmessage")) return;
						
						if (GeneralMethods.isBender(player.getName(), Element.Water)) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_LUNAR_ECLIPSE_MESSAGE);
								} else if (WaterMethods.isFullMoon(world)) {
									player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_FULL_MOONRISE_MESSAGE);
								} else {
									player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_MOONRISE_MESSAGE);
								}
							} else {
								if (WaterMethods.isFullMoon(world)) {
									player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_FULL_MOONRISE_MESSAGE);
								} else {
									player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_MOONRISE_MESSAGE);
								}
							}
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire)) {
							if(player.hasPermission("bending.message.daymessage")) return;
							player.sendMessage(FireMethods.getFireColor() + DEFAULT_SUNSET_MESSAGE);
						}
					}
				}

				if (!times.get(world) && FireMethods.isDay(world)) {
					// The hashmap says it is night, but it is day.
					times.put(world, true);
					if (GeneralMethods.hasRPG()) {
						if (RPGMethods.isSozinsComet(world)) {
							events.put(world, WorldEvents.SozinsComet.toString());
						} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.SolarEclipse.toString());
						} else {
							events.put(world, "");
						}
					} else {
						events.put(world, "");
					}
					for (Player player: world.getPlayers()) {
						if (GeneralMethods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(WaterMethods.getWaterColor() + DEFAULT_MOONSET_MESSAGE);
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(FireMethods.getFireColor() + DEFAULT_SOZINS_COMET_MESSAGE);
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(FireMethods.getFireColor() + DEFAULT_SOLAR_ECLIPSE_MESSAGE);
								} else {
									player.sendMessage(FireMethods.getFireColor() + DEFAULT_SUNRISE_MESSAGE);
								}
							} else {
								player.sendMessage(FireMethods.getFireColor() + DEFAULT_SUNRISE_MESSAGE);
							}
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
			ChiComboManager.handleParalysis();
			HorizontalVelocityTracker.updateAll();
			handleCooldowns();
		} catch (Exception e) {
			GeneralMethods.stopBending();
			e.printStackTrace();
		}
	}
}
