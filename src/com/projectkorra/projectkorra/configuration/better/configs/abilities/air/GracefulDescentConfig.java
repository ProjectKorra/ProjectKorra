package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class GracefulDescentConfig extends AbilityConfig {

	public GracefulDescentConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "GracefulDescent";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Passives" };
	}

}