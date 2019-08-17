package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthDomeConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int Height = 0;
	public final double Radius = 0;
	public final double Range = 0;
	
	public EarthDomeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthDome";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth", "Combos" };
	}

}