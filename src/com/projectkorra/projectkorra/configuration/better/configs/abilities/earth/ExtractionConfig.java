package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class ExtractionConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int SelectRange = 0;
	public final int DoubleLootChance = 0;
	public final int TripleLootChance = 0;
	
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