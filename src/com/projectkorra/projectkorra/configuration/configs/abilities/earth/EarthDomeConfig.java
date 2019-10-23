package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthDomeConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final int Height = 5;
	public final double Radius = 4;
	public final double Range = 25;
	
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