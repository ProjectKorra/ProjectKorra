package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.rpg.RPGMethods;

public class BendingManager implements Runnable {

	private static BendingManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time

	public BendingManager() {
		instance = this;
		this.time = System.currentTimeMillis();
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (final UUID uuid : BendingPlayer.getPlayers().keySet()) {
			final BendingPlayer bPlayer = BendingPlayer.getPlayers().get(uuid);
			for (final String abil : bPlayer.getCooldowns().keySet()) {
				if (System.currentTimeMillis() >= bPlayer.getCooldown(abil)) {
					bPlayer.removeCooldown(abil);
				}
			}
		}
	}

	public void handleDayNight() {
		for (final World world : Bukkit.getServer().getWorlds()) {
			if (!this.times.containsKey(world)) {
				if (ElementalAbility.isDay(world)) {
					this.times.put(world, true);
				} else {
					this.times.put(world, false);
				}
			} else {
				if (GeneralMethods.hasRPG()) {
					if (RPGMethods.isFullMoon(world) || RPGMethods.isLunarEclipse(world) || RPGMethods.isSolarEclipse(world) || RPGMethods.isSozinsComet(world)) {
						continue;
					}
				}
				if (this.times.get(world) && !ElementalAbility.isDay(world)) {
					// The hashmap says it is day, but it is not.
					this.times.put(world, false); // Sets time to night.
					for (final Player player : world.getPlayers()) {
						final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
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

				if (!this.times.get(world) && ElementalAbility.isDay(world)) {
					// The hashmap says it is night, but it is day.
					this.times.put(world, true);
					for (final Player player : world.getPlayers()) {
						final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
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

	@Override
	public void run() {
		try {
			this.interval = System.currentTimeMillis() - this.time;
			this.time = System.currentTimeMillis();
			ProjectKorra.time_step = this.interval;

			CoreAbility.progressAll();
			TempPotionEffect.progressAll();
			this.handleDayNight();
			RevertChecker.revertAirBlocks();
			HorizontalVelocityTracker.updateAll();
			this.handleCooldowns();
			TempArmor.cleanup();

			for (final Player player : Bukkit.getOnlinePlayers()) {
				if (Bloodbending.isBloodbent(player)) {
					ActionBar.sendActionBar(Element.BLOOD.getColor() + "* Bloodbent *", player);
				} else if (MetalClips.isControlled(player)) {
					ActionBar.sendActionBar(Element.METAL.getColor() + "* MetalClipped *", player);
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSunriseMessage() {
		return ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Extras.Fire.DayMessage"));
	}

	public static String getSunsetMessage() {
		return ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Extras.Fire.NightMessage"));
	}

	public static String getMoonriseMessage() {
		return ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Extras.Water.NightMessage"));
	}

	public static String getMoonsetMessage() {
		return ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Extras.Water.DayMessage"));
	}

}
