package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.entity.Player;

public abstract class LavaAbility extends EarthAbility implements SubAbility {
	
	public LavaAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public Element getElement() {
		return Element.LAVA;
	}
	
}
