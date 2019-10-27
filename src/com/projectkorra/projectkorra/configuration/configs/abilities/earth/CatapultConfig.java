package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CatapultConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final double StageTimeMult = 2.5;
	public final boolean CancelWithAngle = true;
	public final double Angle = 20;
	
	public final long AvatarState_Cooldown = 2500;
	
	public CatapultConfig() {
		super(true, "Catapult is an advanced earthbending ability that allows you to forcefully push yourself using earth, reaching great heights. This technique is best used when travelling, but it can also be used to quickly escape a battle.", "Hold sneak until you see particles and hear a sound and then release to be propelled in the direction you're looking. Additionally, you can left-click to be propelled with less power.");
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