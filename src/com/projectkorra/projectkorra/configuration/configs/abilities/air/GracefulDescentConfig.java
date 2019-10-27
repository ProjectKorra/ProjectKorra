package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class GracefulDescentConfig extends AbilityConfig {

	public GracefulDescentConfig() {
		super(true, "GracefulDescent is a passive ability which allows airbenders to make a gentle landing, negating all fall damage on any surface.", null);
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