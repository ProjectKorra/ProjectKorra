package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class DensityShiftConfig extends AbilityConfig {

	public final long Duration = 500;
	
	public DensityShiftConfig() {
		super(true, "DensityShift is a passive ability which allows earthbenders to make a firm landing negating all fall damage on any earthbendable surface.", null);
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