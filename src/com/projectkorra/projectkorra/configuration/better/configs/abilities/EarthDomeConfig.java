package com.projectkorra.projectkorra.configuration.better.configs.abilities;

public class EarthDomeConfig extends AbilityConfig {

	public EarthDomeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthDome";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}