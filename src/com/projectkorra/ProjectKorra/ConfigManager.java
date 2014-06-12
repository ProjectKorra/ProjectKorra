package com.projectkorra.ProjectKorra;

import java.util.ArrayList;

public class ConfigManager {

	static ProjectKorra plugin;
	
	public ConfigManager(ProjectKorra plugin) {
		ConfigManager.plugin = plugin;
		configCheck();
	}
	
	public static void configCheck() {
			    
		ArrayList<String> earthbendable = new ArrayList<String>();
		earthbendable.add("STONE");
		earthbendable.add("COAL_ORE");
		earthbendable.add("DIAMOND_ORE");
		earthbendable.add("DIRT");
		earthbendable.add("GOLD_ORE");
		earthbendable.add("GRASS");
		earthbendable.add("GRAVEL");
		earthbendable.add("IRON_ORE");
		earthbendable.add("LAPIS_ORE");
		earthbendable.add("NETHERRACK");
		earthbendable.add("REDSTONE_ORE");
		earthbendable.add("SAND");
		earthbendable.add("SANDSTONE");
		
		plugin.getConfig().addDefault("Properties.GlobalCooldown", 500);
		
		plugin.getConfig().addDefault("Properties.Air.CanBendWithWeapons", false);
		
		plugin.getConfig().addDefault("Properties.Water.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Water.NightFactor", 1.5);
		
		plugin.getConfig().addDefault("Properties.Earth.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Earth.EarthbendableBlocks", earthbendable);
		
		plugin.getConfig().addDefault("Properties.Fire.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Fire.NightFactor", 1.5);
		
		plugin.getConfig().addDefault("Properties.Chi.CanBendWithWeapons", true);
		
		plugin.getConfig().addDefault("Abilities.Air.Passive.Factor", 0.3);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Speed", 2);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Jump", 3);
		
		plugin.getConfig().addDefault("Abilities.Water.Passive.SwimSpeedFactor", 0.7);
		plugin.getConfig().addDefault("Abilities.Water.Plantbending.RegrowTime", 180000);
		
		plugin.getConfig().addDefault("Abilities.Earth.Passive.Duration", 2500);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Speed", 1);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Jump", 2);
				
		plugin.getConfig().addDefault("Storage.engine", "sqlite");
		
		plugin.getConfig().addDefault("Storage.MySQL.host", "localhost");
		plugin.getConfig().addDefault("Storage.MySQL.port", 3306);
		plugin.getConfig().addDefault("Storage.MySQL.pass", "");
		plugin.getConfig().addDefault("Storage.MySQL.db", "minecraft");
		plugin.getConfig().addDefault("Storage.MySQL.user", "root");
		
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}
}
