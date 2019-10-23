package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireBurstConfig extends AbilityConfig {

	public final long Cooldown = 4500;
	public final long ChargeTime = 2500;
	public final boolean Ignite = true;
	public final double Damage = 4;
	public final double Range = 20;
	public final double AngleTheta = 10;
	public final double AnglePhi = 10;
	public final double ParticlesPercentage = 5;
	
	public final long AvatarState_Cooldown = 2000;
	public final long AvatarState_ChargeTime = 1000;
	public final double AvatarState_Damage = 6;
	
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