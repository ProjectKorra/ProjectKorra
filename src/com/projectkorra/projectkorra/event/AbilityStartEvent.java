package com.projectkorra.projectkorra.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Called when an ability starts
 * 
 * @author Philip
 *
 */
public class AbilityStartEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	boolean cancelled = false;
	Ability ability;

	public AbilityStartEvent(Ability ability) {
		this.ability = ability;
	}

	public Ability getAbility() {
		return ability;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
