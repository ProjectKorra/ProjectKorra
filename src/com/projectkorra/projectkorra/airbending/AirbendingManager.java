package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.ProjectKorra;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;

	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		AirBlast.progressOrigins();
		AirBubble.handleBubbles();
		AirSuction.progressOrigins();
	}

}
