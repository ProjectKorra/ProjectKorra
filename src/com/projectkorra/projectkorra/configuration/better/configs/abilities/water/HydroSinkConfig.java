package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class HydroSinkConfig extends AbilityConfig {

	public HydroSinkConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "HydroSink";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water", "Passives" };
	}

}