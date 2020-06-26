package com.projectkorra.projectkorra.firebending.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;

public class FirebendingManager implements Runnable {

	public ProjectKorra plugin;

	public FirebendingManager(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		FireDamageTimer.handleFlames();
		FireAbility.removeFire();
	}
}
