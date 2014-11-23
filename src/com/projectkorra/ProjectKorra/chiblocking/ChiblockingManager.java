package com.projectkorra.ProjectKorra.chiblocking;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class ChiblockingManager implements Runnable {

	public ProjectKorra plugin;
	
	public ChiblockingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			ChiPassive.handlePassive();
			Smokescreen.removeFromHashMap(player);
			WarriorStance.progressAll();
		}
	}

}