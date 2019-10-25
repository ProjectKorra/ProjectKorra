package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireWheelConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final double Damage = 3;
	public final double Range = 30;
	public final double Speed = 25;
	public final double FireTicks = 1;
	public final int Height = 2;
	
	public final double AvatarState_Damage = 3000;
	public final double AvatarState_Range = 30;
	public final double AvatarState_Speed = 25;
	public final double AvatarState_FireTicks = 1;
	public final int AvatarState_Height = 2;
	
	public FireWheelConfig() {
		super(true, "A high-speed wheel of fire that travels along the ground for long distances dealing high damage.", "FireShield (Hold Shift) > Right Click a block in front of you twice > Switch to Blaze > Release Shift");
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