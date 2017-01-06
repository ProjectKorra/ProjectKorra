package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

public class WaterbendingManager implements Runnable {

	public ProjectKorra plugin;

	public WaterbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		//WaterPassive.handlePassive(); # Fast Swim is now managed in FastSwim.java
		Torrent.progressAllCleanup();
		WaterArms.progressAllCleanup();
	}

}
