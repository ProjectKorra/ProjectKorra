package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModifier;

public class AttributeModifyEvent extends Event {

	private final CoreAbility ability;
	private final String attribute;
	private final double oldValue;
	private double newValue;
	private final AttributeModifier modifier;

	public AttributeModifyEvent(final CoreAbility ability, final String attribute, final double oldValue, final double newValue, final AttributeModifier modifier) {
		this.ability = ability;
		this.attribute = attribute;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.modifier = modifier;
	}

	public CoreAbility getAbility() {
		return this.ability;
	}

	public AttributeModifier getModifier() {
		return this.modifier;
	}

	public boolean hasModifier() {
		return this.modifier != null;
	}

	public String getAttribute() {
		return this.attribute;
	}

	public double getOldValue() {
		return this.oldValue;
	}

	public double getNewValue() {
		return this.newValue;
	}

	public void setNewValue(final double newValue) {
		this.newValue = newValue;
	}

	@Override
	public HandlerList getHandlers() {
		return null;
	}

}
