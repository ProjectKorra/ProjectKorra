package com.projectkorra.projectkorra.configuration.better.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class ChiAgilityConfig extends AbilityConfig {

	public final int JumpPower = 0;
	public final int SpeedPower = 0;
	
	public ChiAgilityConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "ChiAgility";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi", "Passives" };
	}

}