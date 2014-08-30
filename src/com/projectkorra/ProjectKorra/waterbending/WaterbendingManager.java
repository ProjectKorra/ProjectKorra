package com.projectkorra.ProjectKorra.waterbending;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class WaterbendingManager implements Runnable {

	public ProjectKorra plugin;
	
	public WaterbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		WaterPassive.handlePassive();
		Plantbending.regrow();
		Bloodbending.progressAll();
		WaterSpout.handleSpouts(Bukkit.getServer());
		FreezeMelt.handleFrozenBlocks();
		OctopusForm.progressAll();
		Torrent.progressAll();
		TorrentBurst.progressAll();
		HealingWaters.heal(Bukkit.getServer());
		WaterReturn.progressAll();
		WaterManipulation.progressAll();
		WaterWall.progressAll();
		Wave.progressAll();
		IceSpike.progressAll();
		IceSpike2.progressAll();
	}

}
