package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirBurstConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ChargeTime = 1750;
	public final double FallHeightThreshold = 15;
	public final double PushFactor = 2.0;
	public final double Damage = 0;
	public final double AnglePhi = 10;
	public final double AngleTheta = 10;
	public final int ChargeParticles = 10;
	public final double ParticlePercentage = 50;
	
	public final long AvatarState_ChargeTime = 500;
	public final double AvatarState_Damage = 4;
	
	public AirBurstConfig() {
		super(true, "AirBurst creates an outward expanding sphere of air that knocks back everything in its path.", "Sneak until particles appear then\n Left Click to direct the burst forward or \n Release Sneak");
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