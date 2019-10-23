package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSpoutConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final long Interval = 100;
	public final double Height = 15;
	
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