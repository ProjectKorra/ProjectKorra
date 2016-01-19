package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.entity.Player;

public abstract class CombustionAbility extends FireAbility implements SubAbility {
	
	public CombustionAbility(Player player) {
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
