package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.aikar.timings.lib.MCTiming;

import com.projectkorra.projectkorra.event.WorldTimeEvent;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
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
	private final HashMap<World, WorldTimeEvent.Time> times = new HashMap<>(); // true if day time

	public BendingManager() {
		instance = this;
		handleDayNight();
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (Map.Entry<UUID, BendingPlayer> entry : BendingPlayer.getPlayers().entrySet()) {
			BendingPlayer bPlayer = entry.getValue();

			bPlayer.removeOldCooldowns();
		}
	}

	public void handleDayNight() {
		for (final World world : Bukkit.getServer().getWorlds()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(world.getName())) {
				continue;
			}

			WorldTimeEvent.Time from = this.times.get(world);

			WorldTimeEvent.Time to = ElementalAbility.isDay(world) ? WorldTimeEvent.Time.DAY : WorldTimeEvent.Time.NIGHT;

			if (from == null) {
				this.times.put(world, to);
				continue;
			}

			if (from != to) {
				WorldTimeEvent event = new WorldTimeEvent(world, from, to);
				Bukkit.getPluginManager().callEvent(event);

				this.times.put(world, to);
			}
		}
	}

	@Override
	public void run() {
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

		TempFallingBlock.manage();

		final long currentTime = System.currentTimeMillis();
		while (!TempBlock.REVERT_QUEUE.isEmpty()) {
			final TempBlock tempBlock = TempBlock.REVERT_QUEUE.peek(); //Check if the top TempBlock is ready for reverting
			if (currentTime >= tempBlock.getRevertTime()) {
				TempBlock.REVERT_QUEUE.poll();
				tempBlock.revertBlock();
			} else {
				break;
			}
		}
	}

	public static String getSunriseMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Fire.DayMessage"));
	}

	public static String getSunsetMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Fire.NightMessage"));
	}

	public static String getMoonriseMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Water.NightMessage"));
	}

	public static String getMoonsetMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Water.DayMessage"));
	}

}
