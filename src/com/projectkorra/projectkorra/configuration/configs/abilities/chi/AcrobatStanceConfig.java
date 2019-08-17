package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AcrobatStanceConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final int Speed = 0;
	public final int Jump = 0;
	public final double ChiBlockBoost = 0;
	public final double ParalyzeChanceDecrease = 0;
	
	public AcrobatStanceConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AcrobatStance";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}