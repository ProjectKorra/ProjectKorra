package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class SmokescreenConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int Duration = 0;
	public final double Radius = 0;
	
	public SmokescreenConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Smokescreen";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}