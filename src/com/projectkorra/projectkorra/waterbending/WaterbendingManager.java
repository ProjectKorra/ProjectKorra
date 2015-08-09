package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;

public class WaterbendingManager implements Runnable {

	public ProjectKorra plugin;

	public WaterbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		WaterPassive.handlePassive();
		Plantbending.regrow();
		PlantArmor.progressAll();
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
		IceBlast.progressAll();
		WaterWave.progressAll();
		WaterCombo.progressAll();
		WaterArms.progressAll();
		VineWhip.repeat();
	}

}
