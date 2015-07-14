package com.projectkorra.ProjectKorra.airbending;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;
	
	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		AirBlast.progressAll();
		AirPassive.handlePassive(Bukkit.getServer());
		AirBurst.progressAll(StockAbilities.AirBurst);
		AirScooter.progressAll(StockAbilities.AirScooter);
		Suffocate.progressAll();
		AirSpout.progressAll(StockAbilities.AirSpout);
		AirBubble.handleBubbles(Bukkit.getServer());
		AirSuction.progressAll();
		AirSwipe.progressAll(StockAbilities.AirSwipe);
		Tornado.progressAll();
		AirShield.progressAll(StockAbilities.AirShield);
		AirCombo.progressAll();
		FlightAbility.progressAll(StockAbilities.Flight);
	}

}
