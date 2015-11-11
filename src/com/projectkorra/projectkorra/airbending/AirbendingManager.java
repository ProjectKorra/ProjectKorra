package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;

	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		AirBlast.progressAll();
		AirPassive.handlePassive(Bukkit.getServer());
		AirBurst.progressAll();
		AirScooter.progressAll();
		Suffocate.progressAll();
		AirSpout.progressAll();
		AirBubble.handleBubbles(Bukkit.getServer());
		AirSuction.progressAll();
		AirSwipe.progressAll();
		Tornado.progressAll();
		AirShield.progressAll();
		AirCombo.progressAll();
		FlightAbility.progressAll();
	}

}
