package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ProjectKorra;

public class FirebendingManager implements Runnable {

	public ProjectKorra plugin;

	public FirebendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		FireStream.handleDissipation();
		Enflamed.handleFlames();
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
		FireMethods.removeFire();
		HeatControl.progressAll();
		FireStream.dissipateAll();
		FireStream.progressAll();
		FireCombo.progressAll();
	}
}
