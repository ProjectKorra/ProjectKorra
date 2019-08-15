package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirSpoutConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final long Interval = 0;
	public final double Height = 0;
	
	public final double AvatarState_Height = 0;
	
	public AirSpoutConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirSpout";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}