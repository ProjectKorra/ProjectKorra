package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireWheelConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Damage = 0;
	public final double Range = 0;
	public final double Speed = 0;
	public final double FireTicks = 0;
	public final int Height = 0;
	
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Range = 0;
	public final double AvatarState_Speed = 0;
	public final double AvatarState_FireTicks = 0;
	public final int AvatarState_Height = 0;
	
	public FireWheelConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "FireWheel";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire", "Combos" };
	}

}