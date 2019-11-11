package com.projectkorra.projectkorra.ability.legacy;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import org.bukkit.entity.Player;

public abstract class MetalAbility<Info extends AbilityInfo, C extends AbilityConfig> extends EarthAbility<Info, C> implements SubAbility {

	public MetalAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
}
