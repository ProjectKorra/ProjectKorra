package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityEndEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	Ability ability;

	public AbilityEndEvent(final Ability ability) {
		this.ability = ability;
	}

	public Ability getAbility() {
		return this.ability;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
