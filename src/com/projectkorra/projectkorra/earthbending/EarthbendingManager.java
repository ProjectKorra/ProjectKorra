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
		EarthPassive.sandSpeed();
		RevertChecker.revertEarthBlocks();
		EarthTunnel.progressAll();
		EarthArmor.moveArmorAll();
		Catapult.progressAll();
		Tremorsense.manage(Bukkit.getServer());
		EarthColumn.progressAll();
		CompactColumn.progressAll();
		Shockwave.progressAll();
		EarthBlast.progressAll();
		MetalClips.progressAll();
		LavaSurge.progressAll();
		LavaFlow.progressAll();
		EarthSmash.progressAll();
		SandSpout.spoutAll();
	}
}
