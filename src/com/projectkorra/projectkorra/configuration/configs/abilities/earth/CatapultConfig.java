package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CatapultConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final double StageTimeMult = 2.5;
	public final boolean CancelWithAngle = true;
	public final double Angle = 20;
	
	public final long AvatarState_Cooldown = 2500;
	
	public CatapultConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Catapult";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}