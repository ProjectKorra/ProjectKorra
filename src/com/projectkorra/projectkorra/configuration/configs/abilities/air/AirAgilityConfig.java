package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirAgilityConfig extends AbilityConfig {

	public final int JumpPower = 0;
	public final int SpeedPower = 0;
	
	public AirAgilityConfig() {
		super(true, "", null);
	}

	@Override
	public String getName() {
		return "AirAgility";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Passives" };
	}

}