package com.projectkorra.projectkorra.airbending.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirSuction;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;

	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		AirBlast.progressOrigins();
		AirSuction.progressOrigins();
	}

}
