package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WarriorStanceConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final int Strength = 1;
	public final int Resistance = -1;
	
	public WarriorStanceConfig() {
		super(true, "WarriorStance is an advanced chiblocker technique that gives the chiblocker increased damage but makes them a tad more vulnerable. This ability is useful when finishing off weak targets.", "Left click to activate WarriorStance. Additionally, left click to disable it.");
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