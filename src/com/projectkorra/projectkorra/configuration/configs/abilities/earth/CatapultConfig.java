package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CatapultConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double StageTimeMult = 0;
	public final boolean CancelWithAngle = true;
	public final double Angle = 0;
	
	public final long AvatarState_Cooldown = 0;
	
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