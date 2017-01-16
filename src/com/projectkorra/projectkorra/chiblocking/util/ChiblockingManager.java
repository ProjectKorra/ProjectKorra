package com.projectkorra.projectkorra.chiblocking.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ChiblockingManager implements Runnable {
	public ProjectKorra plugin;

	public ChiblockingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Smokescreen.removeFromHashMap(player);
			if (Paralyze.isParalyzed(player)) {
				if (player.getLocation().subtract(0, 0.1, 0).getBlock().getType().equals(Material.AIR)) {
					player.setVelocity(new Vector(0, -0.4, 0));
				}
				player.setFallDistance(0F);
			}
		}
	}

}
