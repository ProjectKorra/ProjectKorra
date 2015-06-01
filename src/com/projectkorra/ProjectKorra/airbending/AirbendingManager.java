package com.projectkorra.ProjectKorra.airbending;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;
	
	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		try {
			AirBlast.progressAll();
			AirPassive.handlePassive(Bukkit.getServer());
			AirBurst.progressAll();
			AirScooter.progressAll();
			Suffocate.progressAll();
			AirSpout.spoutAll();
			AirBubble.handleBubbles(Bukkit.getServer());
			AirSuction.progressAll();
			AirSwipe.progressAll();
			Tornado.progressAll();
			AirShield.progressAll();
			AirCombo.progressAll();
			FlightAbility.progressAll();
		} catch (Exception e) {
			GeneralMethods.logError(e, false);
		}
	}

}
