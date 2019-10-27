package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirAgilityConfig extends AbilityConfig {

	public final int JumpPower = 2;
	public final int SpeedPower = 3;
	
	public AirAgilityConfig() {
		super(true, "Airbenders passively manipulate the air around them allowing them to run faster and jump higher.", null);
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