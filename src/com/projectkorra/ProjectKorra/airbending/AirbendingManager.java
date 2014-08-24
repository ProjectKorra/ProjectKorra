package com.projectkorra.ProjectKorra.airbending;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.ProjectKorra;

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
		BreathSphere.progressAll();
		AirSpout.spoutAll();
		AirBubble.handleBubbles(Bukkit.getServer());
		AirSuction.progressAll();
		for (int ID : AirSwipe.instances.keySet()) {
			AirSwipe.progress(ID);
		}
		for (int ID : Tornado.instances.keySet()) {
			Tornado.progress(ID);
		}
		for (int ID : AirShield.instances.keySet()) {
			AirShield.progress(ID);
		}
	}

}
