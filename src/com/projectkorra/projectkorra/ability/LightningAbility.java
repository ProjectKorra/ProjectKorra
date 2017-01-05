package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.entity.Player;

public abstract class LightningAbility extends FireAbility implements SubAbility {

	public LightningAbility(Player player) {
		super(player);
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
