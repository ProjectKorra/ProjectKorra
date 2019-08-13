package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirBurstConfig extends AbilityConfig {

	public final long ChargeTime = 0;
	public final double FallHeightThreshold = 0;
	public final double PushFactor = 0;
	public final double Damage = 0;
	public final long Cooldown = 0;
	public final double AnglePhi = 0;
	public final double AngleTheta = 0;
	public final int ChargeParticles = 0;
	public final double ParticlePercentage = 0;
	
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Damage = 0;
	
	public AirBurstConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirBurst";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}