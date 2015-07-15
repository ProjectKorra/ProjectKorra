package com.projectkorra.ProjectKorra;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Objects.HorizontalVelocityTracker;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.configuration.ConfigLoadable;
import com.projectkorra.ProjectKorra.firebending.FireMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

public class BendingManager implements Runnable, ConfigLoadable {

	private static BendingManager instance;
	
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	private static String sozinsCometMessage = config.getString("Properties.Fire.CometMessage");
	private static String solarEclipseMessage = config.getString("Properties.Fire.SolarEclipseMessage");

	private static String sunriseMessage = config.getString("Properties.Fire.DayMessage");
	private static String sunsetMessage = config.getString("Properties.Fire.NightMessage");

	private static String moonriseMessage = config.getString("Properties.Water.NightMessage");
	private static String fullMoonriseMessage = config.getString("Properties.Water.FullMoonMessage");
	private static String lunarEclipseMessage = config.getString("Properties.Water.LunarEclipsetMessage");
	private static String moonsetMessage = config.getString("Properties.Water.DayMessage");
	
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
									player.sendMessage(WaterMethods.getWaterColor() + lunarEclipseMessage);
								} else if (WaterMethods.isFullMoon(world)) {
									player.sendMessage(WaterMethods.getWaterColor() + fullMoonriseMessage);
								} else {
									player.sendMessage(WaterMethods.getWaterColor() + moonriseMessage);
								}
							} else {
								if (WaterMethods.isFullMoon(world)) {
									player.sendMessage(WaterMethods.getWaterColor() + fullMoonriseMessage);
								} else {
									player.sendMessage(WaterMethods.getWaterColor() + moonriseMessage);
								}
							}
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire)) {
							if(player.hasPermission("bending.message.daymessage")) return;
							player.sendMessage(FireMethods.getFireColor() + sunsetMessage);
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
							player.sendMessage(WaterMethods.getWaterColor() + moonsetMessage);
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(FireMethods.getFireColor() + sozinsCometMessage);
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(FireMethods.getFireColor() + solarEclipseMessage);
								} else {
									player.sendMessage(FireMethods.getFireColor() + sunriseMessage);
								}
							} else {
								player.sendMessage(FireMethods.getFireColor() + sunriseMessage);
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

	@Override
	public void reloadVariables() {
		sozinsCometMessage = config.getString("Properties.Fire.CometMessage");
		solarEclipseMessage = config.getString("Properties.Fire.SolarEclipseMessage");

		sunriseMessage = config.getString("Properties.Fire.DayMessage");
		sunsetMessage = config.getString("Properties.Fire.NightMessage");

		moonriseMessage = config.getString("Properties.Water.NightMessage");
		fullMoonriseMessage = config.getString("Properties.Water.FullMoonMessage");
		lunarEclipseMessage = config.getString("Properties.Water.LunarEclipsetMessage");
		moonsetMessage = config.getString("Properties.Water.DayMessage");
	}
	
}
