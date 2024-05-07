package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Called when an entity is killed by
 * DamageHandler#damageEntity(final Entity entity, Player source, double damage, final Ability ability, boolean ignoreArmor)
 */

public class EntityBendingDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();

	private final Entity entity;
	private final Ability ability;
	private final double damage;

	/**
	 * Creates a new EntityBendingDeathEvent
	 * @param entity the entity who died
	 * @param damage the amount of damage done in the attack that killed the
	 *            victim
	 * @param ability the ability used to kill the entity
	 */
	public EntityBendingDeathEvent(final Entity entity, final double damage, final Ability ability) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
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

	/**
	 *
	 * @return the amount of damage done in the attack that killed the victim
	 */
	public double getDamage() {
		return this.damage;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
