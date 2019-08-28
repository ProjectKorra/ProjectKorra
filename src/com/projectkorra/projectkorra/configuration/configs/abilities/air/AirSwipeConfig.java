package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSwipeConfig extends AbilityConfig {
	
	public final long Cooldown = 1250;
	public final int AnimationParticleAmount = 2;
	public final int Arc = 16;
	public final int StepSize = 4;
	public final long MaxChargeTime = 2500;
	public final double Damage = 2;
	public final double PushFactor = .5;
	public final double Speed = 25;
	public final double Range = 15;
	public final double Radius = 1.5;
	public final double MaxChargeFactor = 3;
	
	public final long AvatarState_Cooldown = 700;
	public final double AvatarState_Damage = 3;
	public final double AvatarState_PushFactor = 1.0;
	public final double AvatarState_Range = 20;
	public final double AvatarState_Radius = 2;
	
	public AirSwipeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirSwipe";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}