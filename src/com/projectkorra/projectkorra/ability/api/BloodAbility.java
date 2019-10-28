package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public abstract class BloodAbility<Info extends AbilityInfo, C extends AbilityConfig> extends WaterAbility<Info, C> implements SubAbility {

	public BloodAbility(final Player player) {
		super(player);
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
