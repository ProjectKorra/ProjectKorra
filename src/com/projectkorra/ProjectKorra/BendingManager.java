package com.projectkorra.ProjectKorra;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Objects.HorizontalVelocityTracker;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.firebending.FireMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	public static HashMap<World, String> events = new HashMap<>(); // holds any current event.

	static final String DEFAULT_SOZINS_COMET_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Fire.CometMessage");
	static final String DEFAULT_SOLAR_ECLIPSE_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Fire.SolarEclipseMessage");

	static final String DEFAULT_SUNRISE_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Fire.DayMessage");
	static final String DEFAULT_SUNSET_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Fire.NightMessage");

	static final String DEFAULT_MOONRISE_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Water.NightMessage");
	static final String DEFAULT_FULL_MOONRISE_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Water.FullMoonMessage");
	static final String DEFAULT_LUNAR_ECLIPSE_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Water.LunarEclipsetMessage");
	static final String DEFAULT_MOONSET_MESSAGE = ProjectKorra.plugin.getConfig().getString("Properties.Water.DayMessage");
	
	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<>(); // true if day time

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
		time = System.currentTimeMillis();
	}

	public void handleCooldowns() {
		for (String bP: BendingPlayer.players.keySet()) {
			BendingPlayer bPlayer = BendingPlayer.players.get(bP);
			bPlayer.cooldowns.keySet().stream()
					.filter(abil -> System.currentTimeMillis() >= bPlayer.cooldowns.get(abil))
					.forEach(bPlayer::removeCooldown);
		}
	}

	public void handleDayNight() {
		Bukkit.getServer().getWorlds().stream()
				.filter(world -> !events.containsKey(world))
				.forEach(world -> events.put(world, ""));
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
