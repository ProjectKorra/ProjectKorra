package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class BlueFireAbility extends FireAbility implements SubAbility {

	public BlueFireAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.BLUE_FIRE;
	}

	public static double getDamageFactor() {
		return ConfigManager.defaultConfig.get().getDouble("Properties.Fire.BlueFire.DamageFactor");
	}

	public static double getCooldownFactor() {
		return ConfigManager.defaultConfig.get().getDouble("Properties.Fire.BlueFire.CooldownFactor");
	}

	public static double getRangeFactor() {
		return ConfigManager.defaultConfig.get().getDouble("Properties.Fire.BlueFire.RangeFactor");
	}

}
