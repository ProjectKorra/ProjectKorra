package com.projectkorra.ProjectKorra;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;

public class ProjectKorra extends JavaPlugin {

	public static ProjectKorra plugin;
	public static Logger log;

	@Override
	public void onEnable() {
		ProjectKorra.log = this.getLogger();
		plugin = this;

		new Methods(this);
		getServer().getPluginManager().registerEvents(new PKListener(this), this);

		new Commands(this);
		new AbilityModuleManager(this);
		new ConfigManager(this);

		DBConnection.host = getConfig().getString("Storage.MySQL.host");
		DBConnection.port = getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = getConfig().getString("Storage.MySQL.db");
		DBConnection.user = getConfig().getString("Storage.MySQL.user");

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BendingManager(this), 0, 1);

		DBConnection.init();
		for (Player player: Bukkit.getOnlinePlayers()) {
			Methods.createBendingPlayer(player.getUniqueId(), player.getName());
		}
	}

	@Override
	public void onDisable() {
		for (Player player: Bukkit.getOnlinePlayers()) {
			Methods.saveBendingPlayer(player.getName());
		}
		DBConnection.sql.close();
	}
}
