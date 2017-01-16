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
	private Entity entity;
	private Ability ability;
	private double damage;
	private boolean ignoreArmor;

	/**
	 * Create a new AbilityDamageEntityEvent
	 * 
	 * @param entity The entity that was damaged
	 * @param ability The damaging ability
	 * @param damage The amount of damage done
	 */
	public AbilityDamageEntityEvent(Entity entity, Ability ability, double damage, boolean ignoreArmor) {
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
		return damage;
	}

	/**
	 * Sets the damage dealt to the entity
	 * 
	 * @param damage the amount of damage done
	 */
	public void setDamage(double damage) {
		this.damage = damage;
	}

	/**
	 * Gets the entity that was damaged
	 * 
	 * @return the damaged entity
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Gets the ability used
	 * 
	 * @return ability used
	 */
	public Ability getAbility() {
		return ability;
	}

	public boolean doesIgnoreArmor() {
		return ignoreArmor;
	}

	/**
	 * Gets the player that used the ability
	 * 
	 * @return player that used ability
	 */
	public Player getSource() {
		return ability.getPlayer();
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
