package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class ImmobilizeConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ParalyzeDuration = 0;
	
	public ImmobilizeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Paralyze";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi", "Combos" };
	}

}