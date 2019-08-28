package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class RapidPunchConfig extends AbilityConfig {

	public final long Cooldown = 3500;
	public final long Interval = 500;
	public final double DamagePerPunch = 2;
	public final int TotalPunches = 3;
	
	public RapidPunchConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "RapidPunch";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}