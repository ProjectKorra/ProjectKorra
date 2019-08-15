package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class SuffocateConfig extends AbilityConfig {
	
	public final long Cooldown = 0;
	public final boolean RequireConstantAim = true;
	public final double ConstantAimRadius = 0;
	public final boolean CanSuffocateUndead = false;
	public final int AnimationParticleAmount = 0;
	public final double AnimationSpeed = 0;
	public final long ChargeTime = 0;
	public final double Range = 0;
	public final double AnimationRadius = 0;
	public final double Damage = 0;
	public final double DamageInitialDelay = 0;
	public final double DamageInterval = 0;
	public final int SlownessPotency = 0;
	public final double SlownessInitialDelay = 0;
	public final double SlownessInterval = 0;
	public final int BlindnessPotency = 0;
	public final double BlindnessInitialDelay = 0;
	public final double BlindnessInterval = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Range = 0;
	
	public SuffocateConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Suffocate";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}