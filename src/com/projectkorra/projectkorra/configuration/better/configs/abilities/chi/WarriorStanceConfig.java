package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class WarriorStanceConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final int Strength = 0;
	public final int Resistance = 0;
	
	public WarriorStanceConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "WarriorStance";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}