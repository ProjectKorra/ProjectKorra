package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import org.bukkit.entity.Player;

public abstract class ChiAbility<Info extends AbilityInfo, C extends AbilityConfig> extends ElementalAbility<Info, C> {

	public ChiAbility(final Player player) {
		super(player);
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
