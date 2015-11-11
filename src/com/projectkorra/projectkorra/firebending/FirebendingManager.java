package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class FirebendingManager implements Runnable {

	public ProjectKorra plugin;

	public FirebendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		FirePassive.handlePassive();
		FireJet.progressAll();
		Cook.progressAll();
		Illumination.progressAll();
		FireBlast.progressAll();
		Fireball.progressAll();
		FireBurst.progressAll();
		FireShield.progressAll();
		Lightning.progressAll();
		WallOfFire.progressAll();
		Combustion.progressAll();
		for (Block block : FireStream.ignitedblocks.keySet()) {
			if (block.getType() != Material.FIRE) {
				FireStream.ignitedblocks.remove(block);
			}
		}
		FireMethods.removeFire();
		HeatControl.progressAll();
		FireStream.dissipateAll();
		FireStream.progressAll();
		FireCombo.progressAll();
	}
}
