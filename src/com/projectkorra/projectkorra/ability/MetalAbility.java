package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;
import org.bukkit.entity.Player;

public abstract class MetalAbility extends EarthAbility implements SubAbility {

	public MetalAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.METAL;
	}

}
