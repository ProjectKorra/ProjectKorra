package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ChiAgilityConfig extends AbilityConfig {

	public final int JumpPower = 2;
	public final int SpeedPower = 2;
	
	public ChiAgilityConfig() {
		super(true, "", null);
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