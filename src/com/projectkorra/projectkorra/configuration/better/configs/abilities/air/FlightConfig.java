package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class FlightConfig extends AbilityConfig {

	public final long Duration = 0;
	public final long Cooldown = 0;
	public final double BaseSpeed = 0;
	
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