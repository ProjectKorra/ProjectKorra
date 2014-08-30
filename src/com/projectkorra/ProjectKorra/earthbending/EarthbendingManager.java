package com.projectkorra.ProjectKorra.earthbending;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
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
		LavaWall.progressAll();
		LavaWave.progressAll();
	}
}
