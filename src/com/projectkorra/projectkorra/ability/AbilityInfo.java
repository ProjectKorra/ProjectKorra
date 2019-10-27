package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.loader.AbilityLoader;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.element.Element;

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

	public String getName() {
		return this.abilityData.name();
	}

	public Element getElement() {
		return this.abilityLoader.getElement();
	}

	public AbilityLoader getLoader() {
		return this.abilityLoader;
	}
}
