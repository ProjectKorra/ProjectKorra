package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.projectkorra.projectkorra.event.WorldTimeEvent;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
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
	private final TempBlock.TempBlockRevertTask tempBlockRevertTask = new TempBlock.TempBlockRevertTask();
	public BendingManager() {
		instance = this;
		this.time = System.currentTimeMillis();

		times.clear();

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

				//RPG will handle its own day/night messages, so don't run PK Core ones if RPG exists
				if (GeneralMethods.getRPG() == null) {
					for (final Player player : world.getPlayers()) {
						final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (bPlayer == null) continue;

						if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.daymessage") && to == WorldTimeEvent.Time.DAY) {
							String s = getMoonsetMessage();
							player.sendMessage(Element.WATER.getColor() + s);
						}
						else if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.nightmessage") && to == WorldTimeEvent.Time.NIGHT) {
							String s = getMoonriseMessage();
							player.sendMessage(Element.WATER.getColor() + s);
						}

						if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.nightmessage") && to == WorldTimeEvent.Time.NIGHT) {
							String s = getSunsetMessage();
							player.sendMessage(Element.FIRE.getColor() + s);
						}
						else if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.daymessage") && to == WorldTimeEvent.Time.DAY) {
							String s = getSunriseMessage();
							player.sendMessage(Element.FIRE.getColor() + s);
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

		//try (MCTiming timing = this.CORE_ABILITY_TIMING.startTiming()) {
		CoreAbility.progressAll();
		//}

		//try (MCTiming timing = this.TEMP_POTION_TIMING.startTiming()) {
		TempPotionEffect.progressAll();
		//}

		//try (MCTiming timing = this.DAY_NIGHT_TIMING.startTiming()) {
		this.handleDayNight();
		//}

		RevertChecker.revertAirBlocks();

		//try (MCTiming timing = this.HORIZONTAL_VELOCITY_TRACKER_TIMING.startTiming()) {
		HorizontalVelocityTracker.updateAll();
		//}

		//try (MCTiming timing = this.COOLDOWN_TIMING.startTiming()) {
		this.handleCooldowns();
		//}

		//try (MCTiming timing = this.TEMP_ARMOR_TIMING.startTiming()) {
		TempArmor.cleanup();
		//}

		//try (MCTiming timing = this.ACTIONBAR_STATUS_TIMING.startTiming()) {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (Bloodbending.isBloodbent(player)) {
				ActionBar.sendActionBar(Element.BLOOD.getColor() + "* Bloodbent *", player);
			} else if (MetalClips.isControlled(player)) {
				ActionBar.sendActionBar(Element.METAL.getColor() + "* MetalClipped *", player);
			}
		}
		//}

		//try (MCTiming timing = this.TEMP_FALLING_BLOCK_TIMING.startTiming()) {
		TempFallingBlock.manage();
		//}

		//try (MCTiming timing = this.TEMP_BLOCK_TIMING.startTiming()) {
		tempBlockRevertTask.run();
		//}
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
