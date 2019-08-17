package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FastSwimConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double SpeedFactor = 0;
	
	public FastSwimConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "FastSwim";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water", "Passives" };
	}

}