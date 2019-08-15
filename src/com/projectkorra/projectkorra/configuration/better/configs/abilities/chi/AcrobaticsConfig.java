package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AcrobaticsConfig extends AbilityConfig {

	public final double FallReductionFactor = 0;
	
	public AcrobaticsConfig() {
		super(true, "", "");
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