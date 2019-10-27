package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AcrobaticsConfig extends AbilityConfig {

	public final double FallReductionFactor = 0.75;
	
	public AcrobaticsConfig() {
		super(true, "Acrobatics is a passive ability which negates all fall damage based on a percent chance.", null);
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