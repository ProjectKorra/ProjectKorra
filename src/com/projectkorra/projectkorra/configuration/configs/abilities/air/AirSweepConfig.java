package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSweepConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final double Damage = 4;
	public final double Range = 20;
	public final double Speed = 1.5;
	public final double Knockback = 3.5;
	
	public final double AvatarState_Damage = 6;
	public final double AvatarState_Range = 25;
	public final double AvatarState_Knockback = 5.0;
	
	public AirSweepConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirSweep";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Combos" };
	}

}