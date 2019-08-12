package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public abstract class LightningAbility<C extends AbilityConfig> extends FireAbility<C> implements SubAbility {

	public LightningAbility(final C config, final Player player) {
		super(config, player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.LIGHTNING;
	}

}
