package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirStreamConfig extends AbilityConfig {

	public final long Cooldown = 7000;
	public final double Range = 30;
	public final double Speed = 0.5;
	
	public final double EntityCarryHeight = 15;
	public final long EntityCarryDuration = 5000;
	
	public final double AvatarState_Range = 40;
	public final double AvatarState_EntityCarryHeight = 40;
	public final long AvatarState_EntityCarryDuration = 20000;
	
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