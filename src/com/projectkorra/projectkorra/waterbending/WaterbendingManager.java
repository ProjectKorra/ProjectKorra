package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.ProjectKorra;

public class WaterbendingManager implements Runnable {

	public ProjectKorra plugin;

	public WaterbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		WaterPassive.handlePassive();
		PhaseChangeFreeze.handleFrozenBlocks();
		HealingWaters.heal();
		Torrent.progressAllCleanup();
		WaterArms.progressAllCleanup();
	}

}
