package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Called when an ability damages an {@link Entity}
 *
 * @author kingbirdy
 *
 */
public class AbilityDamageEntityEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled = false;
	private final Entity entity;
	private final Ability ability;
	private double damage;
	private final boolean ignoreArmor;

	/**
	 * Create a new AbilityDamageEntityEvent
	 *
	 * @param entity The entity that was damaged
	 * @param ability The damaging ability
	 * @param damage The amount of damage done
	 */
	public AbilityDamageEntityEvent(final Entity entity, final Ability ability, final double damage, final boolean ignoreArmor) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
		this.ignoreArmor = ignoreArmor;
	}

	/**
	 * Returns the damage dealt to the entity
	 *
	 * @return the amount of damage done
	 */
	public double getDamage() {
		return this.damage;
	}

	/**
	 * Sets the damage dealt to the entity
	 *
	 * @param damage the amount of damage done
	 */
	public void setDamage(final double damage) {
		this.damage = damage;
	}

	/**
	 * Gets the entity that was damaged
	 *
	 * @return the damaged entity
	 */
	public Entity getEntity() {
		return this.entity;
	}

	/**
	 * Gets the ability used
	 *
	 * @return ability used
	 */
	public Ability getAbility() {
		return this.ability;
	}

	public boolean doesIgnoreArmor() {
		return this.ignoreArmor;
	}

	/**
	 * Gets the player that used the ability
	 *
	 * @return player that used ability
	 */
	public Player getSource() {
		return this.ability.getPlayer();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}
}
