package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class ChiSaturationConfig extends AbilityConfig {

	public final double ExhaustionFactor = 0;
	
	public ChiSaturationConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "ChiSaturation";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi", "Passives" };
	}

}