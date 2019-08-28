package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CombustionConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final double Damage = 6;
	public final double Range = 40;
	public final double Radius = 1.5;
	public final double Speed = 40;
	public final float ExplosivePower = 1.2F;
	public final boolean BreakBlocks = true;
	
	public CombustionConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Combustion";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}