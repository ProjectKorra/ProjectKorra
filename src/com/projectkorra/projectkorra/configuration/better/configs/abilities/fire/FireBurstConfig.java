package com.projectkorra.projectkorra.configuration.better.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class FireBurstConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ChargeTime = 0;
	public final boolean Ignite = true;
	public final double Damage = 0;
	public final double Range = 0;
	public final double AngleTheta = 0;
	public final double AnglePhi = 0;
	public final double ParticlesPercentage = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Damage = 0;
	
	public FireBurstConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "FireBurst";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}