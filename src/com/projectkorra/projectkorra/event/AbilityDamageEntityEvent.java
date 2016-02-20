package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Called when an ability damages an {@link Entity}
 * @author kingbirdy
 *
 */
public class AbilityDamageEntityEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private boolean cancelled = false;
	private Entity entity;
	private Ability ability;
	double damage;
	
	/**
	 * Create a new AbilityDamageEntityEvent
	 * @param entity The entity that was damaged
	 * @param ability The damaging ability
	 * @param damage The amount of damage done
	 */
	public AbilityDamageEntityEvent(Entity entity, Ability ability, double damage) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
	}
	
	/**
	 * Returns the damage dealt to the entity
	 * @return the amount of damage done
	 */
	public double getDamage() {
		return damage;
	}

	/**
	 * Sets the damage dealt to the entity
	 * @param damage the amount of damage done
	 */
	public void setDamage(double damage) {
		this.damage = damage;
	}

	/**
	 * Gets the entity that was damaged
	 * @return the damaged entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * 
	 * @return
	 */
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
