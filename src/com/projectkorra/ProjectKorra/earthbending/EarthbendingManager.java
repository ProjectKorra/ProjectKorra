package com.projectkorra.ProjectKorra.earthbending;

import org.bukkit.Bukkit;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.RevertChecker;

public class EarthbendingManager implements Runnable {

	public ProjectKorra plugin;

	public EarthbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		EarthPassive.revertSands();
		EarthPassive.handleMetalPassives();
		RevertChecker.revertEarthBlocks();
		EarthTunnel.progressAll();
		EarthArmor.moveArmorAll();
		Tremorsense.manage(Bukkit.getServer());
		Catapult.progressAll();
		EarthColumn.progressAll();
		CompactColumn.progressAll();
		Shockwave.progressAll();
		EarthBlast.progressAll();
		LavaSurge.progressAll();
		LavaFlow.progressAll();
	}
}
