package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterBubbleConfig extends AbilityConfig {

	public final long ClickDuration = 1000;
	public final double Radius = 6;
	public final double Speed = 10;
	public final boolean MustStartAboveWater = true;
	
	public WaterBubbleConfig() {
		super(true, "WaterBubble is a basic waterbending ability that allows the bender to create air pockets under water. This is incredibly useful for building under water.", "Hold sneak when in range of water to push the water back and create a water bubble. Alternatively, you can click to create a bubble for a short amount of time.");
	}

	@Override
	public String getName() {
		return "WaterBubble";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}