package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class FirebendingManager implements Runnable {

	public ProjectKorra plugin;
	
	public FirebendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		FirePassive.handlePassive();
		FireJet.progressAll();
		Cook.progressAll();
		Illumination.manage(Bukkit.getServer());
		FireBlast.progressAll();
		Fireball.progressAll();
		FireBurst.progressAll();
		FireShield.progressAll();
		Lightning.progressAll();
		WallOfFire.manage();
		Combustion.progressAll();
		for (Block block : FireStream.ignitedblocks.keySet()) {
			if (block.getType() != Material.FIRE) {
				FireStream.ignitedblocks.remove(block);
			}
		}
		FireStream.dissipateAll();
		FireStream.progressAll();
		FireCombo.progressAll();
	}
}
