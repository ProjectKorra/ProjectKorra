package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class ShockwaveConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ChargeTime = 0;
	public final double FallThreshold = 0;
	public final double Range = 0;
	public final double Damage = 0;
	public final double Knockback = 0;
	public final double Angle = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Range = 0;
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Knockback = 0;
	
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