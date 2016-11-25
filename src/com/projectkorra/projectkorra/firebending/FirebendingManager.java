package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;

public class FirebendingManager implements Runnable {

	public ProjectKorra plugin;

	public FirebendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		BlazeArc.handleDissipation();
		FireDamageTimer.handleFlames();
		FirePassive.handlePassive();
		BlazeArc.dissipateAll();
		FireAbility.removeFire();
	}
}
