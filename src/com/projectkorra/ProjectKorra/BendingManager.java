package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.airbending.AirPassive;
import com.projectkorra.ProjectKorra.airbending.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.firebending.FirePassive;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	ArrayList<World> worlds = new ArrayList<World>();
	ConcurrentHashMap<World, Boolean> nights = new ConcurrentHashMap<World, Boolean>();
	ConcurrentHashMap<World, Boolean> days = new ConcurrentHashMap<World, Boolean>();

	static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		try {
			AirPassive.handlePassive(Bukkit.getServer());
			ChiPassive.handlePassive();
			WaterPassive.handlePassive();
			FirePassive.handlePassive();
			EarthPassive.revertSands();
			Plantbending.regrow();
			handleDayNight();
		} catch (Exception e) {
			Methods.stopBending();
			e.printStackTrace();
		}
	}
	
	public void handleDayNight() {
		for (World world: plugin.getServer().getWorlds()) {
			if (world.getWorldType() == WorldType.NORMAL && !worlds.contains(world)) {
				worlds.add(world);
				nights.put(world, false);
				days.put(world, false);
			}
		}
		ArrayList<World> removeworlds = new ArrayList<World>();
		for (World world: worlds) {
			if (!plugin.getServer().getWorlds().contains(world)) {
				removeworlds.add(world);
				continue;
			}
			boolean night = nights.get(world);
			boolean day = days.get(world);
			if (Methods.isDay(world) && !day) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
						player.sendMessage(ChatColor.RED + defaultsunrisemessage);
					}
				}
				days.replace(world, true);
			}
			
			if (!Methods.isDay(world) && day) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
						player.sendMessage(ChatColor.RED + defaultsunsetmessage);
					}
				}
				days.replace(world, false);
			}
			
			if (Methods.isNight(world) && !night) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
						player.sendMessage(ChatColor.AQUA + defaultmoonrisemessage);
					}
				}
				nights.replace(world, true);
			}
			
			if (!Methods.isNight(world) && night) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
						player.sendMessage(ChatColor.AQUA + defaultmoonsetmessage);
					}
				}
				nights.replace(world, false);
			}
		}
		
		for (World world: removeworlds) {
			worlds.remove(world);
		}
		
	}
}
