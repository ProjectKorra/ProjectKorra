package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.songoda.kingdoms.main.Config;

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
		return Config.getConfig().getDouble("Properties.Fire.BlueFire.DamageFactor");
	}

	public static double getCooldownFactor() {
		return Config.getConfig().getDouble("Properties.Fire.BlueFire.CooldownFactor");
	}

	public static double getRangeFactor() {
		return Config.getConfig().getDouble("Properties.Fire.BlueFire.CooldownFactor");
	}

}
