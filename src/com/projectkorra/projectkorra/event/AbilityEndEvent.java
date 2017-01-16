package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

public class AbilityEndEvent extends Event {
	private final HandlerList handlers = new HandlerList();

	Ability ability;

	public AbilityEndEvent(Ability ability) {
		this.ability = ability;
	}

	public Ability getAbility() {
		return ability;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
