package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.GeneralMethods;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is killed by {@link GeneralMethods#damageEntity(Player player, Entity entity, double damage, String ability) GeneralMethods.damageEntity}
 * 
 * @author kingbirdy
 */

public class PlayerBendingDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Player victim;
	private Player attacker;
	private String ability;
	private double damage;

	/**
	 * 
	 * @param victim the player who died
	 * @param attacker the player who killed the victim
	 * @param damage the amount of damage done in the attack that killed the victim
	 * @param ability the ability used to kill the victim
	 */
	public PlayerBendingDeathEvent(Player victim, Player attacker, double damage, String ability) {
		this.victim = victim;
		this.attacker = attacker;
		this.ability = ability;
		this.damage = damage;
	}

	/**
	 * 
	 * @return the player who was killed
	 */
	public Player getVictim() {
		return victim;
	}

	/**
	 * 
	 * @return the player who killed the victim
	 */
	public Player getAttacker() {
		return attacker;
	}

	/**
	 * 
	 * @return the ability used to kill the victim
	 */
	public String getAbility() {
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
