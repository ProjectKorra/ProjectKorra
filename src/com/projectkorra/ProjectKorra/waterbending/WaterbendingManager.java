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
		for (int ID : WaterManipulation.instances.keySet()) {
			WaterManipulation.progress(ID);
		}

		for (int ID : WaterWall.instances.keySet()) {
			WaterWall.progress(ID);
		}

		for (int ID : Wave.instances.keySet()) {
			Wave.progress(ID);
		}

		for (int ID : IceSpike.instances.keySet()) {
			IceSpike.instances.get(ID).progress();
		}
		IceSpike2.progressAll();
	}

}
