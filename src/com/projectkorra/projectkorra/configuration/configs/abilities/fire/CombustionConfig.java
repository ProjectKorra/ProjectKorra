package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CombustionConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Damage = 0;
	public final double Range = 0;
	public final double Radius = 0;
	public final double Speed = 0;
	public final float ExplosivePower = 0;
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