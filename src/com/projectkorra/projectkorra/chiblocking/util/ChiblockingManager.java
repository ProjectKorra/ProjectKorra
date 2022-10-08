package com.projectkorra.projectkorra.chiblocking.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChiblockingManager implements Runnable {
	public ProjectKorra plugin;

	public ChiblockingManager(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			Smokescreen.removeFromHashMap(player);
		}
	}

}
