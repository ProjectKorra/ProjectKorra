package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirStreamConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Range = 0;
	public final double Speed = 0;
	
	public final double EntityCarryHeight = 0;
	public final long EntityCarryDuration = 0;
	
	public final double AvatarState_Range = 0;
	public final double AvatarState_EntityCarryHeight = 0;
	public final long AvatarState_EntityCarryDuration = 0;
	
	public AirStreamConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirStream";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Combos" };
	}

}