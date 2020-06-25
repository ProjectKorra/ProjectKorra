package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Called when an entity is killed by Bending
 */

public class EntityBendingDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private final Entity entity;
	private final Ability ability;

	/**
	 * Creates a new EntityBendingDeathEvent
	 *
	 * @param entity the entity who died
	 * @param ability the ability used to kill the entity
	 */
	public EntityBendingDeathEvent(final Entity entity, final Ability ability) {
		this.entity = entity;
		this.ability = ability;
	}

	/**
	 *
	 * @return the entity that was killed
	 */
	public Entity getEntity() {
		return this.entity;
	}

	/**
	 *
	 * @return the player who killed the entity
	 */
	public Player getAttacker() {
		return this.ability.getPlayer();
	}

	/**
	 *
	 * @return the ability used to kill the victim
	 */
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
