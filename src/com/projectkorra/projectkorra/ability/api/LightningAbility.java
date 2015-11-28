package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class LightningAbility extends FireAbility implements SubAbility {

	public LightningAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public LightningAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}
}
