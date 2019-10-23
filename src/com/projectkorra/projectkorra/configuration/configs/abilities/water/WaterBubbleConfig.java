package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterBubbleConfig extends AbilityConfig {

	public final long ClickDuration = 1000;
	public final double Radius = 6;
	public final double Speed = 10;
	public final boolean MustStartAboveWater = true;
	
	public WaterBubbleConfig() {
		super(true, "", "");
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