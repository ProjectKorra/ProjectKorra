package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ShockwaveConfig extends AbilityConfig {

	public final long Cooldown = 4000;
	public final long ChargeTime = 2500;
	public final double FallThreshold = 15;
	public final double Range = 20;
	public final double Damage = 4;
	public final double Knockback = 1.2;
	public final double Angle = 40;
	
	public final long AvatarState_Cooldown = 1000;
	public final long AvatarState_ChargeTime = 500;
	public final double AvatarState_Range = 40;
	public final double AvatarState_Damage = 6;
	public final double AvatarState_Knockback = 2.0;
	
	public ShockwaveConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Shockwave";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}