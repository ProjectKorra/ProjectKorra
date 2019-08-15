package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class HighJumpConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Height = 0;
	
	public HighJumpConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "HighJump";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}