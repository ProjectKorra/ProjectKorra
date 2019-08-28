package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AcrobaticsConfig extends AbilityConfig {

	public final double FallReductionFactor = 0.75;
	
	public AcrobaticsConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "AcrobatStance";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi", "Passives" };
	}

}