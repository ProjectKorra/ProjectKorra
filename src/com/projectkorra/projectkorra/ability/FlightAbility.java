package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.entity.Player;

public abstract class FlightAbility extends AirAbility implements SubAbility {

	public FlightAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return AirAbility.class;
	}
	
	@Override
	public Element getElement() {
		return Element.FLIGHT;
	}
	
}
