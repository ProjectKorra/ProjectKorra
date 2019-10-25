package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireSpinConfig extends AbilityConfig {

	public final long Cooldown = 4500;
	public final double Damage = 3;
	public final double Range = 12;
	public final double Speed = 25;
	public final double Knockback = 1.5;
	
	public final double AvatarState_Damage = 6;
	public final double AvatarState_Range = 20;
	public final double AvatarState_Knockback = 2.0;
	
	public FireSpinConfig() {
		super(true, "A circular array of fire that causes damage and massive knockback to nearby enemies.", "FireBlast > FireBlast > FireShield (Left Click) > FireShield (Tap Shift");
	}

	@Override
	public String getName() {
		return "FireSpin";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire", "Combos" };
	}

}