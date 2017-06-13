package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModifier;

public class AttributeModifyEvent extends Event {
	
	private CoreAbility ability;
	private String attribute;
	private double oldValue; 
	private double newValue;
	private AttributeModifier modifier;
	
	public AttributeModifyEvent(CoreAbility ability, String attribute, double oldValue, double newValue, AttributeModifier modifier) {
		this.ability = ability;
		this.attribute = attribute;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.modifier = modifier;
	}
	
	public CoreAbility getAbility() {
		return ability;
	}
	
	public AttributeModifier getModifier() {
		return modifier;
	}
	
	public boolean hasModifier() {
		return modifier != null;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public double getOldValue() {
		return oldValue;
	}
	
	public double getNewValue() {
		return newValue;
	}
	
	public void setNewValue(double newValue) {
		this.newValue = newValue;
	}
	
	@Override
	public HandlerList getHandlers() {
		return null;
	}

}
