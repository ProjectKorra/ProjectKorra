package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ExtractionConfig extends AbilityConfig {

	public final long Cooldown = 2000;
	public final int SelectRange = 15;
	public final int DoubleLootChance = 25;
	public final int TripleLootChance = 15;
	
	public ExtractionConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Extraction";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}