package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class LightningAbility extends FireAbility implements SubAbility {

	public LightningAbility(final Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.LIGHTNING;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	//Overriding these methods to make sure Lightning abilities don't get buffed by blue fire
	@Override
	public double applyModifiersDamage(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public double applyModifiersRange(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public long applyModifiersCooldown(long value) {
		return (long) GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
	}

}
