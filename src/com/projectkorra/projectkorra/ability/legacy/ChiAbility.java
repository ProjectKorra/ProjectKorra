package com.projectkorra.projectkorra.ability.legacy;

import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import org.bukkit.entity.Player;

public abstract class ChiAbility<Handler extends AbilityHandler> extends ElementalAbility<Handler> {

	public ChiAbility(final Handler abilityHandler, final Player player) {
		super(abilityHandler, player);
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
}
