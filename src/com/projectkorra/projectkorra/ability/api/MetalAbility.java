package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class MetalAbility extends EarthAbility implements SubAbility {

	public MetalAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public MetalAbility(Player player) {
		super(player, false);
	}
	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

}
