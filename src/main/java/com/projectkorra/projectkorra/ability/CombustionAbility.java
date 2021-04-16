package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class CombustionAbility extends FireAbility implements SubAbility {

	public CombustionAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.COMBUSTION;
	}

}
