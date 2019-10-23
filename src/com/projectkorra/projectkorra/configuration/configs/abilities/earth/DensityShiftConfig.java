package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class DensityShiftConfig extends AbilityConfig {

	public final long Duration = 500;
	
	public DensityShiftConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "DensityShift";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water", "Passives" };
	}

}