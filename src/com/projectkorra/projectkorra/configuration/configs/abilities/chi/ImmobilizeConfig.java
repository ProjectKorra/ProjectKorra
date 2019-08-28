package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ImmobilizeConfig extends AbilityConfig {

	public final long Cooldown = 10000;
	public final long ParalyzeDuration = 4000;
	
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