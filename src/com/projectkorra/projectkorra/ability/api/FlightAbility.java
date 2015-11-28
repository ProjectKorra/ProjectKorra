package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class FlightAbility extends AirAbility implements SubAbility {

	public FlightAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public FlightAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return AirAbility.class;
	}
}
