package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Cancellable event called when an ability would push or alter the velocity of an entity.
 * 
 * the entity can be changed, vector can be modified, 
 * and the ability that caused the change can be accessed.
 *
 * @author dNiym
 *
 */

public class AbilityVelocityAffectEntityEvent extends Event implements Cancellable {

	Entity affected;
	Vector newVector;
	Ability ability;
	boolean cancelled = false;
	
	private static final HandlerList handlers = new HandlerList();
	
	public AbilityVelocityAffectEntityEvent(Ability ability, Entity entity, Vector vector) {
		this.affected = entity;
		this.ability = ability;
		this.newVector = vector;
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
		return handlers;
	}

	public Entity getAffected() {
		return affected;
	}

	public void setAffected(Entity affected) {
		this.affected = affected;
	}

	public Vector getNewVector() {
		return newVector;
	}

	public void setNewVector(Vector newVector) {
		this.newVector = newVector;
	}

	public Ability getAbility() {
		return ability;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
