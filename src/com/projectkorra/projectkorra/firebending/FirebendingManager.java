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
		FireJet.progressAll(FireJet.class);
		Cook.progressAll(Cook.class);
		Illumination.progressAll(Illumination.class);
		FireBlast.progressAll(FireBlast.class);
		Fireball.progressAll(Fireball.class);
		FireBurst.progressAll(FireBurst.class);
		FireShield.progressAll(FireShield.class);
		Lightning.progressAll(Lightning.class);
		WallOfFire.progressAll(WallOfFire.class);
		Combustion.progressAll(Combustion.class);
		for (Block block : FireStream.ignitedblocks.keySet()) {
			if (block.getType() != Material.FIRE) {
				FireStream.ignitedblocks.remove(block);
			}
		}
		FireMethods.removeFire();
		HeatControl.progressAll(HeatControl.class);
		FireStream.dissipateAll();
		FireStream.progressAll(FireStream.class);
		FireCombo.progressAll();
	}
}
