package com.projectkorra.ProjectKorra;

import org.bukkit.configuration.file.FileConfiguration;

public class DeathMessageConfigManager {

	static ProjectKorra plugin;

	public DeathMessageConfigManager(ProjectKorra plugin) {
		DeathMessageConfigManager.plugin = plugin;
		configCheck();
	}

	public static void configCheck() {
		
		FileConfiguration config = ProjectKorra.deathMsgConfig.getConfig();
		
		config.addDefault("Properties.Enabled", true);
		config.addDefault("Air.AirBlast", "example message");
		config.addDefault("Fire.FireBlast", "example message");
		
		config.options().copyDefaults(true);
		ProjectKorra.deathMsgConfig.saveConfig();
	}
}
