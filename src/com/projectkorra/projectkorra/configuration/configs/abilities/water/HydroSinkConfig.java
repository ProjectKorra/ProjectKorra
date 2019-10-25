package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class HydroSinkConfig extends AbilityConfig {

	public HydroSinkConfig() {
		super(true, "Hydrosink is a passive ability for waterbenders enabling them to softly land on any waterbendable surface, cancelling all damage.", null);
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