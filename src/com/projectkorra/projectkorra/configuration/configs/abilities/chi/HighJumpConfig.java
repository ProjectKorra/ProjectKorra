package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class HighJumpConfig extends AbilityConfig {

	public final long Cooldown = 500;
	public final double Height = 1.2;
	
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