package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.Material;

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
        FireStream.ignitedblocks.keySet().stream()
                .filter(block -> block.getType() != Material.FIRE)
                .forEach(FireStream.ignitedblocks::remove);
        HeatControl.progressAll();
		FireStream.dissipateAll();
		FireStream.progressAll();
		FireCombo.progressAll();
	}
}
