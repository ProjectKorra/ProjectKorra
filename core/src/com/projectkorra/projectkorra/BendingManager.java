package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.projectkorra.projectkorra.airbending.util.AirbendingManager;
import com.projectkorra.projectkorra.chiblocking.util.ChiblockingManager;
import com.projectkorra.projectkorra.earthbending.util.EarthbendingManager;
import com.projectkorra.projectkorra.event.WorldTimeEvent;
import com.projectkorra.projectkorra.firebending.util.FirebendingManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import com.projectkorra.projectkorra.util.ThreadUtil;
import com.projectkorra.projectkorra.waterbending.util.WaterbendingManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class BendingManager implements Runnable {

	private static BendingManager instance;
	@Deprecated(since = "1.13.0", forRemoval = true)
	public static HashMap<World, String> events = new HashMap<>(); // holds any current event.

	long time;
	long interval;
	private final HashMap<World, WorldTimeEvent.Time> times = new HashMap<>(); // true if day time
	private final TempBlock.TempBlockRevertTask tempBlockRevertTask = new TempBlock.TempBlockRevertTask();
	public BendingManager() {
		instance = this;
		this.time = System.currentTimeMillis();

		times.clear();

		TempElementsRunnable tempElementsRunnable = new TempElementsRunnable();
		if (ProjectKorra.isFolia()) {

			AirbendingManager air = new AirbendingManager(ProjectKorra.plugin);
			ChiblockingManager chiblocking = new ChiblockingManager(ProjectKorra.plugin);
			EarthbendingManager earth = new EarthbendingManager(ProjectKorra.plugin);
			FirebendingManager fire = new FirebendingManager(ProjectKorra.plugin);
			WaterbendingManager water = new WaterbendingManager(ProjectKorra.plugin);

			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> air.run(), 1, 1);
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> water.run(), 1, 1);
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> earth.run(), 1, 1);
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> fire.run(), 1, 1);
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> chiblocking.run(), 1, 1);

			Bukkit.getAsyncScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> {
				this.tempBlockRevertTask.run();
				TempArmor.cleanup();
				TempFallingBlock.manage();
				this.handleCooldowns();
				tempElementsRunnable.run();
				RevertChecker.revertAirBlocks(); //The revert checker class also needs an instance that is run
					 							 //every tick, but the class needs rewriting to be Folia safe
			}, 100, 100, TimeUnit.MILLISECONDS); // Every 2 ticks, approx. Exact tick isn't important

			Bukkit.getGlobalRegionScheduler().runAtFixedRate(ProjectKorra.plugin, (task) -> handleDayNight(), 1, 5); //Every 5 ticks
		} else {
			Bukkit.getScheduler().runTaskTimerAsynchronously(ProjectKorra.plugin, tempElementsRunnable, 1, 2);
		}
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (Map.Entry<UUID, BendingPlayer> entry : BendingPlayer.getPlayers().entrySet()) {
			BendingPlayer bPlayer = entry.getValue();

			ThreadUtil.ensureEntity(bPlayer.getPlayer(), bPlayer::removeOldCooldowns);
		}
	}

	public void handleDayNight() {
		for (final World world : Bukkit.getServer().getWorlds()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(world.getName())) {
				continue;
			}

			WorldTimeEvent.Time from = this.times.get(world);

			WorldTimeEvent.Time to = ElementalAbility.isDay(world) ? WorldTimeEvent.Time.DAY :
					(ElementalAbility.isNight(world) ? WorldTimeEvent.Time.NIGHT :
							(ElementalAbility.isDusk(world) ? WorldTimeEvent.Time.DUSK : WorldTimeEvent.Time.DAWN));

			if (from == null) { //If the time is null, the server/plugin probably just started, so set the previous time to the previous one
				int ord = to.ordinal() - 1;
				if (ord < 0) ord = WorldTimeEvent.Time.values().length - 1;
				from = WorldTimeEvent.Time.values()[ord];
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

						if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.daymessage") && to != WorldTimeEvent.Time.NIGHT && from == WorldTimeEvent.Time.NIGHT) {
							player.sendMessage(Element.WATER.getColor() + getMoonsetMessage());
						} else if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.nightmessage") && to == WorldTimeEvent.Time.NIGHT) {
							player.sendMessage(Element.WATER.getColor() + getMoonriseMessage());
						}

						if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.nightmessage") && to != WorldTimeEvent.Time.DAY && from == WorldTimeEvent.Time.DAY) {
							player.sendMessage(Element.FIRE.getColor() + getSunsetMessage());
						} else if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.daymessage") && to == WorldTimeEvent.Time.DAY) {
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

		CoreAbility.progressAll(); //Player threads. DONE.
		TempPotionEffect.progressAll(); //Player threads. DONE.
		this.handleDayNight(); //Global region
		RevertChecker.revertAirBlocks(); //Complicated, needs rewriting
		HorizontalVelocityTracker.updateAll(); //Player threads. DONE
		this.handleCooldowns(); //Async thread
		TempArmor.cleanup(); //Async thread

		TempFallingBlock.manage(); //Async.

		tempBlockRevertTask.run(); //Async thread
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

	/**
	 * A runnable that manages temp elements for players.
	 * It runs for online players and only polls the next element that is due to expire.
	 * This runnable runs every 20 ticks (1 second).
	 */
	public static class TempElementsRunnable implements Runnable {
		@Override
		public void run() {
			//Manage Temp elements
			while (!BendingPlayer.TEMP_ELEMENTS.isEmpty()) { //We use a while loop so if multiple expire in the same tick, all are done together
				Pair<Player, Long> pair = BendingPlayer.TEMP_ELEMENTS.peek();

				if (System.currentTimeMillis() > pair.getRight()) { //Check if the top temp element has expired
					BendingPlayer.TEMP_ELEMENTS.poll(); //And if it has, remove from the queue, and recalculate temp elements for that player
					BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(pair.getLeft());
					ThreadUtil.ensureEntity(bPlayer.getPlayer(), () -> bPlayer.recalculateTempElements(false));
				} else {
					break; //Break the loop if the top element hasn't expired, as all elements below it won't have either
				}
			}
		}
	}

}
