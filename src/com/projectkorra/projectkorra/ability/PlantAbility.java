package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class PlantAbility extends WaterAbility implements SubAbility {

	public PlantAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.PLANT;
	}

}
