package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FastSwimConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double SpeedFactor = 0.7;
	
	public FastSwimConfig() {
		super(true, "FastSwim is a passive ability for waterbenders allowing them to travel quickly through the water. Simple hold shift while underwater to propel yourself forward.", null);
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