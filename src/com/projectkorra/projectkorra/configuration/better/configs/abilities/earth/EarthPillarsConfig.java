package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthPillarsConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Radius = 0;
	public final double Knockup = 0;
	public final double Damage = 0;
	public final boolean DealsDamage = true;
	public final double FallHeightThreshold = 0;
	
	public EarthPillarsConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthPillars";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth", "Combos" };
	}

}