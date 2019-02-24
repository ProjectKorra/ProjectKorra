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

	private MCTiming TimingCoreAbilityProgressAll, TimingTempPotionProgressAll, TimingHandleDayNight, TimingHorizontalVelocityTrackerUpdateAll, TimingHandleCoolDowns, TimingTempArmorCleanup, TimingActionBarCheck;

	public BendingManager() {
		instance = this;
		this.time = System.currentTimeMillis();

		TimingCoreAbilityProgressAll = ProjectKorra.timing("CoreAbilityProgressAll");
		TimingTempPotionProgressAll = ProjectKorra.timing("TempPotionProgressAll");
		TimingHandleDayNight = ProjectKorra.timing("HandleDayNight");
		TimingHorizontalVelocityTrackerUpdateAll = ProjectKorra.timing("HorizontalVelocityTrackerUpdateAll");
		TimingHandleCoolDowns = ProjectKorra.timing("HandleCoolDowns");
		TimingTempArmorCleanup = ProjectKorra.timing("TempArmorCleanup");
		TimingActionBarCheck = ProjectKorra.timing("ActionBarCheck");
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

		TimingCoreAbilityProgressAll.startTiming();
		CoreAbility.progressAll();
		TimingCoreAbilityProgressAll.stopTiming();

		TimingTempPotionProgressAll.startTiming();
		TempPotionEffect.progressAll();
		TimingTempPotionProgressAll.stopTiming();

		TimingHandleDayNight.startTiming();
		this.handleDayNight();
		TimingHandleDayNight.stopTiming();

		RevertChecker.revertAirBlocks();

		TimingHorizontalVelocityTrackerUpdateAll.startTiming();
		HorizontalVelocityTracker.updateAll();
		TimingHorizontalVelocityTrackerUpdateAll.stopTiming();

		TimingHandleCoolDowns.startTiming();
		this.handleCooldowns();
		TimingHandleCoolDowns.stopTiming();

		TimingTempArmorCleanup.startTiming();
		TempArmor.cleanup();
		TimingTempArmorCleanup.stopTiming();

		TimingActionBarCheck.startTiming();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (Bloodbending.isBloodbent(player)) {
				ActionBar.sendActionBar(Element.BLOOD.getColor() + "* Bloodbent *", player);
			} else if (MetalClips.isControlled(player)) {
				ActionBar.sendActionBar(Element.METAL.getColor() + "* MetalClipped *", player);
			}
		}
		TimingActionBarCheck.stopTiming();
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
