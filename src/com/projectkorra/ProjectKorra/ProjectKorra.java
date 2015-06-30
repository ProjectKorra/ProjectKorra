package com.projectkorra.ProjectKorra;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboModuleManager;
import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModuleManager;
import com.projectkorra.ProjectKorra.Objects.Preset;
import com.projectkorra.ProjectKorra.Utilities.CraftingRecipes;
import com.projectkorra.ProjectKorra.Utilities.Updater;
import com.projectkorra.ProjectKorra.Utilities.logging.PKLogHandler;
import com.projectkorra.ProjectKorra.airbending.AirbendingManager;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.ChiblockingManager;
import com.projectkorra.ProjectKorra.configuration.ConfigManager;
import com.projectkorra.ProjectKorra.earthbending.EarthbendingManager;
import com.projectkorra.ProjectKorra.firebending.FirebendingManager;
import com.projectkorra.ProjectKorra.waterbending.WaterbendingManager;

public class ProjectKorra extends JavaPlugin {

	public static ProjectKorra plugin;
	public static Logger log;
	public static PKLogHandler handler;
	public static long time_step = 1;
	public Updater updater;
	
	@Override
	public void onEnable() {
		plugin = this;
		ProjectKorra.log = this.getLogger();
		try {
		    File logFolder = new File(getDataFolder(), "Logs");
		    if (!logFolder.exists()) {
		        logFolder.mkdirs();
		    }
		    handler = new PKLogHandler(getDataFolder() + File.separator + "ERROR.log");
			log.getParent().addHandler(handler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		new ConfigManager(this);
		new GeneralMethods(this);
		updater = new Updater(this, "http://projectkorra.com/forum/forums/dev-builds.16/index.rss");
		new Commands(this);
		new AbilityModuleManager(this);
		new MultiAbilityModuleManager();
		new MultiAbilityManager();
		new ComboModuleManager();
		new ComboManager();
		new ChiComboManager();
		new CraftingRecipes(this);

		DBConnection.host = getConfig().getString("Storage.MySQL.host");
		DBConnection.port = getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = getConfig().getString("Storage.MySQL.db");
		DBConnection.user = getConfig().getString("Storage.MySQL.user");
		DBConnection.init();
		if (DBConnection.isOpen() == false) {
			//TODO: Log a proper message displaying database problem, pk will not function
			return;
		}

		getServer().getPluginManager().registerEvents(new PKListener(this), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AirbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new WaterbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new EarthbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new FirebendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChiblockingManager(this), 0, 1);
		getServer().getScheduler().runTaskTimerAsynchronously(this, new RevertChecker(this), 0, 200);
		
		for (Player player: Bukkit.getOnlinePlayers()) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			Preset.loadPresets(player);
		}

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
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
	
	public void stopPlugin() {
		getServer().getPluginManager().disablePlugin(plugin);
	}
}
