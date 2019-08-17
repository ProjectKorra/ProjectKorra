package com.projectkorra.projectkorra.configuration.configs.abilities;

public class EmptyAbilityConfig extends AbilityConfig {

	public EmptyAbilityConfig() {
		super(true, null, null);
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String[] getParents() {
		return null;
	}

}
