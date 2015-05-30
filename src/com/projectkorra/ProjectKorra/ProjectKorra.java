package com.projectkorra.ProjectKorra;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboModuleManager;
import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModuleManager;
import com.projectkorra.ProjectKorra.Objects.Preset;
import com.projectkorra.ProjectKorra.Utilities.CraftingRecipes;
import com.projectkorra.ProjectKorra.airbending.AirbendingManager;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.ChiblockingManager;
import com.projectkorra.ProjectKorra.earthbending.EarthbendingManager;
import com.projectkorra.ProjectKorra.firebending.FirebendingManager;
import com.projectkorra.ProjectKorra.waterbending.WaterbendingManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class ProjectKorra extends JavaPlugin {

	public static long time_step = 1;
	public static ProjectKorra plugin;
	public static Logger log;

	@Override
	public void onEnable() {
		ProjectKorra.log = this.getLogger();
		plugin = this;
		new ConfigManager(this);

		new GeneralMethods(this);
		new Commands(this);
		new AbilityModuleManager(this);
		new MultiAbilityModuleManager();
		new MultiAbilityManager();
		new ComboModuleManager();
		new ComboManager();
		new ChiComboManager();

		ConfigManager.configCheck();

		DBConnection.host = getConfig().getString("Storage.MySQL.host");
		DBConnection.port = getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = getConfig().getString("Storage.MySQL.db");
		DBConnection.user = getConfig().getString("Storage.MySQL.user");

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AirbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new WaterbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new EarthbendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new FirebendingManager(this), 0, 1);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ChiblockingManager(this), 0, 1);

		DBConnection.init();
		if (DBConnection.isOpen() == false) return;
		
		for (Player player: Bukkit.getOnlinePlayers()) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			Preset.loadPresets(player);
		}
		getServer().getPluginManager().registerEvents(new PKListener(this), this);

		
		getServer().getScheduler().runTaskTimerAsynchronously(this, new RevertChecker(this), 0, 200);

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		GeneralMethods.deserializeFile();
		GeneralMethods.startCacheCleaner(GeneralMethods.CACHE_TIME);
		new CraftingRecipes(this);
	}

	@Override
	public void onDisable() {
		GeneralMethods.stopBending();
		if (DBConnection.isOpen == false) return;
		DBConnection.sql.close();
	}
	
	public void stopPlugin() {
		getServer().getPluginManager().disablePlugin(plugin);
	}
}
