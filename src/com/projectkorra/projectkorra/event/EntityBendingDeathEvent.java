package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an entity is killed by
 * {@link GeneralMethods#damageEntity(Player player, Entity entity, double damage, String ability)
 * GeneralMethods.damageEntity}
 */

public class EntityBendingDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Entity entity;
	private Ability ability;
	private double damage;

	/**
	 * Creates a new EntityBendingDeathEvent
	 * 
	 * @param entity the entity who died
	 * @param damage the amount of damage done in the attack that killed the
	 *            victim
	 * @param ability the ability used to kill the entity
	 */
	public EntityBendingDeathEvent(Entity entity, double damage, Ability ability) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
	}

	/**
	 * 
	 * @return the entity that was killed
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * 
	 * @return the player who killed the entity
	 */
	public Player getAttacker() {
		return ability.getPlayer();
	}

	/**
	 * 
	 * @return the ability used to kill the victim
	 */
	public Ability getAbility() {
		return ability;
	}

	/**
	 * 
	 * @return the amount of damage done in the attack that killed the victim
	 */
	public double getDamage() {
		return damage;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
