package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChiblockingManager implements Runnable {

	public ProjectKorra plugin;

	public ChiblockingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		ChiPassive.handlePassive();
		WarriorStance.progressAll();
		AcrobatStance.progressAll();
		for (Player player : Bukkit.getOnlinePlayers()) {
			Smokescreen.removeFromHashMap(player);
		}
	}

}
