package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ChiSaturationConfig extends AbilityConfig {

	public final double ExhaustionFactor = .5;
	
	public ChiSaturationConfig() {
		super(true, "ChiSaturation is a passive ability which causes chiblockers' hunger to deplete at a slower rate.", null);
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