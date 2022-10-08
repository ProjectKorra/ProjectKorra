package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;
import org.bukkit.entity.Player;

public abstract class ChiAbility extends ElementalAbility {

	public ChiAbility(final Player player) {
		super(player);
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public Element getElement() {
		return Element.CHI;
	}

}
