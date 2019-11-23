package com.projectkorra.projectkorra.ability.abilities.chi;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.module.ModuleManager;

public abstract class ChiAbilityHandler<T extends Ability, U extends AbilityConfig> extends AbilityHandler<T, U> {

	public ChiAbilityHandler(Class<T> abilityClass, Class<U> configClass) {
		super(abilityClass, configClass);
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public Element getElement() {
		return ModuleManager.getModule(ElementManager.class).getChi();
	}
}
