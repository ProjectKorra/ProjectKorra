package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.ability.combo.ComboModuleManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityModuleManager;
import com.projectkorra.projectkorra.airbending.AirbendingManager;
import com.projectkorra.projectkorra.chiblocking.ChiblockingManager;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthbendingManager;
import com.projectkorra.projectkorra.firebending.FirebendingManager;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.MetricsLite;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.Updater;
import com.projectkorra.projectkorra.util.logging.PKLogHandler;
import com.projectkorra.projectkorra.waterbending.WaterbendingManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ProjectKorra extends JavaPlugin {

	public static ProjectKorra plugin;
	public static Logger log;
	public static PKLogHandler handler;
	public static long time_step = 1;
	public Updater updater;
	public AbilityModuleManager abManager;
	@Override
	public void onEnable() {
		plugin = this;
		ProjectKorra.log = this.getLogger();
		try {
			File logFolder = new File(getDataFolder(), "Logs");
			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}
			handler = new PKLogHandler(logFolder + File.separator + "ERROR.%g.log");
			log.getParent().addHandler(handler);
		}
		catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		new ConfigManager();
		new GeneralMethods(this);
		updater = new Updater(this, "http://projectkorra.com/forums/dev-builds.16/index.rss");
		new Commands(this);
		abManager = new AbilityModuleManager(this);
		new MultiAbilityModuleManager();
		new MultiAbilityManager();
		new ComboModuleManager();
		new ComboManager();
		
		Preset.loadExternalPresets();

		DBConnection.host = getConfig().getString("Storage.MySQL.host");
		DBConnection.port = getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = getConfig().getString("Storage.MySQL.db");
		DBConnection.user = getConfig().getString("Storage.MySQL.user");
		DBConnection.init();
		if (DBConnection.isOpen() == false) {
			//Message is logged by DBConnection
			return;
		}

		getServer().getPluginManager().registerEvents(new PKListener(this), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AirbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new WaterbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new EarthbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new FirebendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChiblockingManager(this), 0, 1);
		getServer().getScheduler().runTaskTimerAsynchronously(this, new RevertChecker(this), 0, 200);

		for (Player player : Bukkit.getOnlinePlayers()) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
		}

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		GeneralMethods.deserializeFile();
		GeneralMethods.startCacheCleaner(GeneralMethods.CACHE_TIME);
		updater.checkUpdate();
	}

	@Override
	public void onDisable() {
		GeneralMethods.stopBending();
		if (DBConnection.isOpen != false) {
			DBConnection.sql.close();
		}
		handler.close();
	}

}
