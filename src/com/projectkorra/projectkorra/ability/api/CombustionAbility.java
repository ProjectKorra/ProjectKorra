package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class CombustionAbility extends FireAbility implements SubAbility {

	public CombustionAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public CombustionAbility(Player player) {
		super(player);
	}

	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}
}
