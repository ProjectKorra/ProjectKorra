package com.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DevJoinListener implements Listener{
	
	ProjectKorra plugin;

	public DevJoinListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDevJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(player.getName().equalsIgnoreCase("sampepere") || player.getName().equalsIgnoreCase("MistPhizzle")) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(ProjectKorra.plugin.getConfig().getBoolean("Properties.DeveloperJoinListener")) {
					online.sendMessage(ChatColor.GOLD + "A member of the Project Korra developer team has joined your server! (" + player.getName() + ")");
				}
			}
		}
	}

}
