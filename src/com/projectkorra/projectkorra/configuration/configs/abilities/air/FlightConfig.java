package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FlightConfig extends AbilityConfig {

	public final long Cooldown = 30000;
	public final long Duration = 25000;
	public final double BaseSpeed = 1.25;
	
	public FlightConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Flight";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}