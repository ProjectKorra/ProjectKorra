package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public abstract class BloodAbility<C extends AbilityConfig> extends WaterAbility<C> implements SubAbility {

	public BloodAbility(final C config, final Player player) {
		super(config, player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.BLOOD;
	}

}
