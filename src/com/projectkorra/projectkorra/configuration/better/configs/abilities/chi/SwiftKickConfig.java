package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class SwiftKickConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Damage = 0;
	public final double ChiBlockChance = 0;
	
	public SwiftKickConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "SwiftKick";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}