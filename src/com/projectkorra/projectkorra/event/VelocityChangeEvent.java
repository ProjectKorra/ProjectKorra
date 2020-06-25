package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.VelocityBuilder;

/**
 * Event for velocity changes made by a {@link VelocityBuilder}
 */
public class VelocityChangeEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean cancelled;
	private Entity entity;
	private CoreAbility ability;
	private Vector velocity;

	public VelocityChangeEvent(Entity entity, CoreAbility ability, Vector velocity) {
		this.entity = entity;
		this.ability = ability;
		this.velocity = velocity;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public CoreAbility getAbility() {
		return ability;
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
