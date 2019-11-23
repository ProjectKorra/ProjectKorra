package com.projectkorra.projectkorra.ability.abilities.air.airswipe;

import com.projectkorra.projectkorra.ability.api.air.AirAbilityHandler;

public class AirSwipeHandler extends AirAbilityHandler<AirSwipe, AirSwipeConfig> {

	public AirSwipeHandler(Class<AirSwipe> abilityClass, Class<AirSwipeConfig> configClass) {
		super(abilityClass, configClass);
	}

	@Override
	public String getName() {
		return "AirSwipe";
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return getConfig().Cooldown;
	}
}
