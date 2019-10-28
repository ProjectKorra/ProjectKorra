package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import org.bukkit.entity.Player;

public abstract class FlightAbility<Info extends AbilityInfo, C extends AbilityConfig> extends AirAbility<Info, C> implements SubAbility {

	public FlightAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return AirAbility.class;
	}
}
