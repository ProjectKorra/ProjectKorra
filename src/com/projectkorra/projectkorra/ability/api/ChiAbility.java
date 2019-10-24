package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public abstract class ChiAbility<C extends AbilityConfig> extends ElementalAbility<C> {

	public ChiAbility(final C config, final Player player) {
		super(config, player);
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
		return Element.CHI;
	}

}
