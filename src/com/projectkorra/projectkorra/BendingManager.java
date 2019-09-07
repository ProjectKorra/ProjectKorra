package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.UUID;

import co.aikar.timings.lib.MCTiming;

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

public class BendingManager implements Runnable {

	private static BendingManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time

	private final MCTiming CORE_ABILITY_TIMING, TEMP_POTION_TIMING, DAY_NIGHT_TIMING, HORIZONTAL_VELOCITY_TRACKER_TIMING, COOLDOWN_TIMING, TEMP_ARMOR_TIMING, ACTIONBAR_STATUS_TIMING;

	public BendingManager() {
		instance = this;
		this.time = System.currentTimeMillis();

		this.CORE_ABILITY_TIMING = ProjectKorra.timing("CoreAbility#ProgressAll");
		this.TEMP_POTION_TIMING = ProjectKorra.timing("TempPotion#ProgressAll");
		this.DAY_NIGHT_TIMING = ProjectKorra.timing("HandleDayNight");
		this.HORIZONTAL_VELOCITY_TRACKER_TIMING = ProjectKorra.timing("HorizontalVelocityTracker#UpdateAll");
		this.COOLDOWN_TIMING = ProjectKorra.timing("HandleCooldowns");
		this.TEMP_ARMOR_TIMING = ProjectKorra.timing("TempArmor#Cleanup");
		this.ACTIONBAR_STATUS_TIMING = ProjectKorra.timing("ActionBarCheck");
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (final UUID uuid : BendingPlayer.getPlayers().keySet()) {
			final BendingPlayer bPlayer = BendingPlayer.getPlayers().get(uuid);
			for (final String abil : bPlayer.getCooldowns().keySet()) {
				if (bPlayer.getBoundAbilityName().equals(abil) && Bukkit.getPlayer(uuid) != null) {
					GeneralMethods.displayMovePreview(Bukkit.getPlayer(uuid));
				}
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
		this.interval = System.currentTimeMillis() - this.time;
		this.time = System.currentTimeMillis();
		ProjectKorra.time_step = this.interval;

		try (MCTiming timing = this.CORE_ABILITY_TIMING.startTiming()) {
			CoreAbility.progressAll();
		}

		try (MCTiming timing = this.TEMP_POTION_TIMING.startTiming()) {
			TempPotionEffect.progressAll();
		}

		try (MCTiming timing = this.DAY_NIGHT_TIMING.startTiming()) {
			this.handleDayNight();
		}

		RevertChecker.revertAirBlocks();

		try (MCTiming timing = this.HORIZONTAL_VELOCITY_TRACKER_TIMING.startTiming()) {
			HorizontalVelocityTracker.updateAll();
		}

		try (MCTiming timing = this.COOLDOWN_TIMING.startTiming()) {
			this.handleCooldowns();
		}

		try (MCTiming timing = this.TEMP_ARMOR_TIMING.startTiming()) {
			TempArmor.cleanup();
		}

		try (MCTiming timing = this.ACTIONBAR_STATUS_TIMING.startTiming()) {
			for (final Player player : Bukkit.getOnlinePlayers()) {
				if (Bloodbending.isBloodbent(player)) {
					ActionBar.sendActionBar(Element.BLOOD.getColor() + "* Bloodbent *", player);
				} else if (MetalClips.isControlled(player)) {
					ActionBar.sendActionBar(Element.METAL.getColor() + "* MetalClipped *", player);
				}
			}
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
