package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.logging.Logger;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.CollisionInitializer;
import com.projectkorra.projectkorra.ability.util.CollisionManager;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.airbending.util.AirbendingManager;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.chiblocking.util.ChiblockingManager;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.util.EarthbendingManager;
import com.projectkorra.projectkorra.firebending.util.FirebendingManager;
import com.projectkorra.projectkorra.hooks.PlaceholderAPIHook;
import com.projectkorra.projectkorra.hooks.WorldGuardFlag;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.Metrics;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.StatisticsManager;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.Updater;
import com.projectkorra.projectkorra.waterbending.util.WaterbendingManager;

public class ProjectKorra extends JavaPlugin {

	public static ProjectKorra plugin;
	public static Logger log;
	public static CollisionManager collisionManager;
	public static CollisionInitializer collisionInitializer;
	public static long time_step = 1;
	public Updater updater;
	private BukkitTask revertChecker;
	private static TimingManager timingManager;

	@Override
	public void onEnable() {
		plugin = this;
		ProjectKorra.log = this.getLogger();

		timingManager = TimingManager.of(this);

		new ConfigManager();
		new GeneralMethods(this);
		final boolean checkUpdateOnStartup = ConfigManager.getConfig().getBoolean("Properties.UpdateChecker");
		this.updater = new Updater(this, "https://projectkorra.com/forum/resources/projectkorra-core.1/", checkUpdateOnStartup);
		new Commands(this);
		new MultiAbilityManager();
		new ComboManager();
		new RegionProtection();
		collisionManager = new CollisionManager();
		collisionInitializer = new CollisionInitializer(collisionManager);
		CoreAbility.registerAbilities();
		collisionInitializer.initializeDefaultCollisions();
		collisionManager.startCollisionDetection();

		Preset.loadExternalPresets();

		DBConnection.init();
		if (!DBConnection.isOpen()) {
			return;
		}

		Manager.startup();
		BendingBoardManager.setup();

		this.getServer().getPluginManager().registerEvents(new PKListener(this), this);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AirbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new WaterbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new EarthbendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new FirebendingManager(this), 0, 1);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChiblockingManager(this), 0, 1);
		this.revertChecker = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new RevertChecker(this), 0, 200);

		TempBlock.startReversion();

		for (final Player player : Bukkit.getOnlinePlayers()) {
			PKListener.getJumpStatistics().put(player, player.getStatistic(Statistic.JUMP));

			OfflineBendingPlayer.loadAsync(player.getUniqueId(), true);
			Manager.getManager(StatisticsManager.class).load(player.getUniqueId());
		}

		final Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.AdvancedPie("Elements") {

			@Override
			public HashMap<String, Integer> getValues(final HashMap<String, Integer> valueMap) {
				for (final Element element : Element.getMainElements()) {
					valueMap.put(element.getName(), this.getPlayersWithElement(element));
				}

				return valueMap;
			}

			private int getPlayersWithElement(final Element element) {
				int counter = 0;
				for (final Player player : Bukkit.getOnlinePlayers()) {
					final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
					if (bPlayer != null && bPlayer.hasElement(element)) {
						counter++;
					}
				}

				return counter;
			}
		});

		final double cacheTime = ConfigManager.getConfig().getDouble("Properties.RegionProtection.CacheBlockTime");

		GeneralMethods.deserializeFile();
		RegionProtection.startCleanCacheTask(cacheTime);

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPIHook(this).register();
		}
	}

	@Override
	public void onDisable() {
		this.revertChecker.cancel();
		GeneralMethods.stopBending();
		for (final Player player : this.getServer().getOnlinePlayers()) {
			if (isStatisticsEnabled()) {
				Manager.getManager(StatisticsManager.class).save(player.getUniqueId(), false);
			}
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && isDatabaseCooldownsEnabled()) {
				bPlayer.saveCooldowns(false);
			}
		}
		Manager.shutdown();
		if (DBConnection.isOpen()) {
			DBConnection.sql.close();
		}
	}

	@Override
	public void onLoad() {
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
			WorldGuardFlag.registerBendingWorldGuardFlag();
		}
	}

	public static CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public static void setCollisionManager(final CollisionManager collisionManager) {
		ProjectKorra.collisionManager = collisionManager;
	}

	public static CollisionInitializer getCollisionInitializer() {
		return collisionInitializer;
	}

	public static void setCollisionInitializer(final CollisionInitializer collisionInitializer) {
		ProjectKorra.collisionInitializer = collisionInitializer;
	}

	public static boolean isStatisticsEnabled() {
		return ConfigManager.getConfig().getBoolean("Properties.Statistics");
	}

	public static boolean isDatabaseCooldownsEnabled() {
		return ConfigManager.getConfig().getBoolean("Properties.DatabaseCooldowns");
	}

	public static MCTiming timing(final String name) {
		return timingManager.of(name);
	}
}
