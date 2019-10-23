package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FerroControlConfig extends AbilityConfig {

	public FerroControlConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "FerroControl";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth", "Passives" };
	}

}