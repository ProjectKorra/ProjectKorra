package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class SandAbility extends EarthAbility implements SubAbility {

	public SandAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public SandAbility(Player player) {
		super(player, false);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
}
