package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class BloodbendingConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Range = 0;
	public final double Knockback = 0;
	
	public final boolean CanOnlyBeUsedAtNight = true;
	public final boolean CanBeUsedOnUndeadMobs = false;
	public final boolean CanOnlyBeUsedDuringFullMoon = true;
	public final boolean CanBloodbendOtherBloodbenders = false;
	
	public BloodbendingConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Bloodbending";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}