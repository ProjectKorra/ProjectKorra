package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class SuffocateConfig extends AbilityConfig {
	
	public final long Cooldown = 0;
	public final boolean RequireConstantAim = true;
	public final double ConstantAimRadius = 3;
	public final boolean CanSuffocateUndead = false;
	public final int AnimationParticleAmount = 1;
	public final double AnimationSpeed = 0;
	public final long ChargeTime = 500;
	public final double Range = 20;
	public final double AnimationRadius = 2.0;
	public final double Damage = 2;
	public final double DamageInitialDelay = 2;
	public final double DamageInterval = 1;
	public final int SlownessPotency = 1;
	public final double SlownessInitialDelay = .5;
	public final double SlownessInterval = 1.25;
	public final int BlindnessPotency = 30;
	public final double BlindnessInitialDelay = 2;
	public final double BlindnessInterval = 1.5;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Damage = 2;
	public final double AvatarState_Range = 20;
	
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