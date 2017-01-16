package com.projectkorra.projectkorra.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.util.Collision;

public class AbilityCollisionEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean cancelled;
	private Collision collision;

	public AbilityCollisionEvent(Collision collision) {
		this.collision = collision;
		this.cancelled = false;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Collision getCollision() {
		return collision;
	}

	public void setCollision(Collision collision) {
		this.collision = collision;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
