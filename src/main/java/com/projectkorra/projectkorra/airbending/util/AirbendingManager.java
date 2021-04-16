package com.projectkorra.projectkorra.airbending.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirBlast;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;

	public AirbendingManager(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		AirBlast.progressOrigins();
	}

}
