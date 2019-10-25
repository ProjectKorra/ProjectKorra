package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AcrobatStanceConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final int Speed = 2;
	public final int Jump = 1;
	public final double ChiBlockBoost = 50;
	public final double ParalyzeChanceDecrease = 0;
	
	public AcrobatStanceConfig() {
		super(true, "AcrobatStance gives a Chiblocker a higher probability of blocking a Bender's Chi while granting them a Speed and Jump Boost. It also increases the rate at which the hunger bar depletes.", "To use, simply left click to activate this stance. Left click once more to deactivate it.");
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