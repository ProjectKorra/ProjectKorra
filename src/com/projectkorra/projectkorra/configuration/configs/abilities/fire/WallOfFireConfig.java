package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WallOfFireConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Damage = 0;
	public final long DamageInterval = 0;
	public final int Height = 0;
	public final int Width = 0;
	public final int Range = 0;
	public final long Interval = 0;
	public final double MaxAngle = 0;
	public final double FireTicks = 0;
	
	public final long AvatarState_Duration = 0;
	public final double AvatarState_Damage = 0;
	public final int AvatarState_Height = 0;
	public final int AvatarState_Width = 0;
	public final double AvatarState_FireTicks = 0;
	
	public WallOfFireConfig() {
		super(true, "WallOfFire is an advanced firebending technique that can be used aggressively or defensively. It's incredibly useful when trying to block off opponents from chasing you or to back them into corners.", "Left click to create a fire wall at the location you clicked. This fire wall will damage entities that run into it and deal knockback.");
	}

	@Override
	public String getName() {
		return "WallOfFire";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}