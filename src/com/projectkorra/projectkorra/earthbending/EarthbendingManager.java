package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.RevertChecker;

import org.bukkit.Bukkit;

public class EarthbendingManager implements Runnable {

	public ProjectKorra plugin;

	public EarthbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		EarthPassive.revertSands();
		EarthPassive.handleMetalPassives();
		RevertChecker.revertEarthBlocks();
		Shockwave.progressAll();
		Tremorsense.manage(Bukkit.getServer());
		EarthTunnel.revertAirBlocks();
	}
}
