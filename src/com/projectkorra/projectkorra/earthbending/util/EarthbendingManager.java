package com.projectkorra.projectkorra.earthbending.util;

import org.bukkit.Bukkit;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.earthbending.Shockwave;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.util.RevertChecker;

public class EarthbendingManager implements Runnable {
	public ProjectKorra plugin;

	public EarthbendingManager(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		RevertChecker.revertEarthBlocks();
		Shockwave.progressAll();
		Tremorsense.manage(Bukkit.getServer());
	}
}
