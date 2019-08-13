package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

public class WaterManipulationConfig extends SourcedWaterAbilityConfig {

	public final double SelectRange = 0;
	
	public WaterManipulationConfig() {
		super(true, "", "", true, true);
	}

	@Override
	public String getName() {
		return "WaterManipulation";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}