package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class HealingAbility extends WaterAbility implements SubAbility {

	public HealingAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public HealingAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}
}
