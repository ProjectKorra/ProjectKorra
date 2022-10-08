package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an ability starts
 *
 * @author Philip
 *
 */
public class AbilityProgressEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	Ability ability;

	public AbilityProgressEvent(final Ability ability) {
		this.ability = ability;
	}

	public Ability getAbility() {
		return this.ability;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
