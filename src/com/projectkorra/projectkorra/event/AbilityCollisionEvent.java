package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.ability.util.Collision;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityCollisionEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean cancelled;
	private Collision collision;

	public AbilityCollisionEvent(final Collision collision) {
		this.collision = collision;
		this.cancelled = false;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Collision getCollision() {
		return this.collision;
	}

	public void setCollision(final Collision collision) {
		this.collision = collision;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
