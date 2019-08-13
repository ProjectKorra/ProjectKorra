package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirSwipeConfig extends AbilityConfig {
	
	public final int AnimationParticleAmount = 0;
	public final int Arc = 0;
	public final int StepSize = 0;
	public final long MaxChargeTime = 0;
	public final double Damage = 0;
	public final double PushFactor = 0;
	public final double Speed = 0;
	public final double Range = 0;
	public final double Radius = 0;
	public final double MaxChargeFactor = 0;
	public final long Cooldown = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Damage = 0;
	public final double AvatarState_PushFactor = 0;
	public final double AvatarState_Range = 0;
	public final double AvatarState_Radius = 0;
	
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