package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.loader.AbilityLoader;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AbilityInfo {

	private final Class<? extends Ability> abilityClass;
	private final AbilityData abilityData;
	private final AbilityLoader abilityLoader;
	private final AbilityConfig abilityConfig;

	public AbilityInfo(Class<? extends Ability> abilityClass, AbilityData abilityData, AbilityLoader abilityLoader, AbilityConfig abilityConfig) {
		this.abilityClass = abilityClass;
		this.abilityData = abilityData;
		this.abilityLoader = abilityLoader;
		this.abilityConfig = abilityConfig;
	}

	public Class<? extends Ability> getAbilityClass() {
		return this.abilityClass;
	}

	public AbilityData getAbilityData() {
		return this.abilityData;
	}

	public AbilityLoader getAbilityLoader() {
		return this.abilityLoader;
	}

	public AbilityConfig getAbilityConfig() {
		return this.abilityConfig;
	}
}
