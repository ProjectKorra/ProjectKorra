package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ParalyzeConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	
	public ParalyzeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Paralyze";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}