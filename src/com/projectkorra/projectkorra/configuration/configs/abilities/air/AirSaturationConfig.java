package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSaturationConfig extends AbilityConfig {

	public final double ExhaustionFactor = 0;
	
	public AirSaturationConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "AirSaturation";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Passives" };
	}

}