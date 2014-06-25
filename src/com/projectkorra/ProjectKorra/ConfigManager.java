package com.projectkorra.ProjectKorra;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {

	static ProjectKorra plugin;
	
	public ConfigManager(ProjectKorra plugin) {
		ConfigManager.plugin = plugin;
		configCheck();
	}
	
	public static void configCheck() {
			   
		FileConfiguration config = ProjectKorra.plugin.getConfig();
		
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
		plugin.getConfig().addDefault("Properties.SeaLevel", 62);
		
		plugin.getConfig().addDefault("Properties.Air.CanBendWithWeapons", false);
		
		plugin.getConfig().addDefault("Properties.Water.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Water.NightFactor", 1.5);
		
		plugin.getConfig().addDefault("Properties.Earth.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Earth.EarthbendableBlocks", earthbendable);
		
		plugin.getConfig().addDefault("Properties.Fire.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Fire.DayFactor", 1.5);
		
		plugin.getConfig().addDefault("Properties.Chi.CanBendWithWeapons", true);
		
		plugin.getConfig().addDefault("Abilities.AvatarState.Enabled", true);
		plugin.getConfig().addDefault("Abilities.AvatarState.Description", "The signature ability of the Avatar, this is a toggle. Click to activate to become "
					+ "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from "
					+ "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities "
					+ "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able "
					+ "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar "
					+ "State selected to deactivate it.");

		plugin.getConfig().addDefault("Abilities.Air.Passive.Factor", 0.3);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Speed", 2);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Jump", 3);
		
		config.addDefault("Abilities.Air.AirBlast.Enabled", true);
		config.addDefault("Abilities.Air.AirBlast.Description", "AirBlast is the most fundamental bending technique of an airbender."
				+ " To use, simply left-click in a direction. A gust of wind will be"
				+ " created at your fingertips, launching anything in its path harmlessly back."
				+ " A gust of air can extinguish fires on the ground or on a player, can cool lava, and "
				+ "can flip levers and activate buttons. Additionally, tapping sneak will change the "
				+ "origin of your next AirBlast to your targeted location.");
		config.addDefault("Abilities.Air.AirBlast.Speed", 25);
		config.addDefault("Abilities.Air.AirBlast.Range", 20);
		config.addDefault("Abilities.Air.AirBlast.Radius", 2);
		config.addDefault("Abilities.Air.AirBlast.Push", 3.5);
		
		config.addDefault("Abilities.Air.AirBurst.Enabled", true);
		config.addDefault("Abilities.Air.AirBurst.Description", "AirBurst is one of the most powerful abilities in the airbender's arsenal. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of air in front of you, or click to release the burst in a sphere around you. "
				+ "Additionally, having this ability selected when you land on the ground from a "
				+ "large enough fall will create a burst of air around you.");
		
		plugin.getConfig().addDefault("Abilities.Air.Tornado.Enabled", true);
		plugin.getConfig().addDefault("Abilities.Air.Tornado.Description", "To use, simply sneak (default: shift). "
				+ "This will create a swirling vortex at the targeted location. "
				+ "Any creature or object caught in the vortex will be launched up "
				+ "and out in some random direction. If another player gets caught "
				+ "in the vortex, the launching effect is minimal. Tornado can "
				+ "also be used to transport the user. If the user gets caught in his/her "
				+ "own tornado, his movements are much more manageable. Provided the user doesn't "
				+ "fall out of the vortex, it will take him to a maximum height and move him in "
				+ "the general direction he's looking. Skilled airbenders can scale anything "
				+ "with this ability.");
		config.addDefault("Abilities.Air.Tornado.Radius", 10);
		config.addDefault("Abilities.Air.Tornado.Height", 25);
		config.addDefault("Abilities.Air.Tornado.Range", 25);
		config.addDefault("Abilities.Air.Tornado.MobPushFactor", 1);
		config.addDefault("Abilities.Air.Tornado.PlayerPushFactor", 1);
		
		plugin.getConfig().addDefault("Abilities.Water.Passive.SwimSpeedFactor", 0.7);
		
		config.addDefault("Abilities.Water.Bloodbending.Enabled", true);
		config.addDefault("Abilities.Water.Bloodbending.Description", "This ability was made illegal for a reason. With this ability selected, sneak while "
				+ "targetting something and you will bloodbend that target. Bloodbent targets cannot move, "
				+ "bend or attack. You are free to control their actions by looking elsewhere - they will "
				+ "be forced to move in that direction. Additionally, clicking while bloodbending will "
				+ "launch that target off in the direction you're looking. "
				+ "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.");
		config.addDefault("Abilities.Water.Bloodbending.ThrowFactor", 2);
		config.addDefault("Abilities.Water.Bloodbending.Range", 10);
		
		
		plugin.getConfig().addDefault("Abilities.Water.Plantbending.RegrowTime", 180000);
		
		plugin.getConfig().addDefault("Abilities.Earth.Passive.Duration", 2500);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Speed", 1);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Jump", 2);
		
		config.addDefault("Abilities.Chi.Paralyze.Enabled", true);
		config.addDefault("Abilities.Chi.Paralyze.Description", "Paralyzes the target, making them unable to do anything for a short "
				+ "period of time. This ability has a long cooldown.");
		config.addDefault("Abilities.Chi.Paralyze.Cooldown", 15000);
		config.addDefault("Abilities.Chi.Paralyze.Duration", 2000);
				
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
