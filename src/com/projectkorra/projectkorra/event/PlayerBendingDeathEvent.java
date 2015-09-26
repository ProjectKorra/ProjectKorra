package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.SubElement;

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
	private Element element;
	private SubElement sub;

	/**
	 * 
	 * @param victim the player who died
	 * @param attacker the player who killed the victim
	 * @param damage the amount of damage done in the attack that killed the victim
	 * @param element the element of the ability
	 * @param sub the subelement of the ability
	 * @param ability the ability used to kill the victim
	 */
	public PlayerBendingDeathEvent(Player victim, Player attacker, double damage, Element element, SubElement sub, String ability) {
		this.victim = victim;
		this.attacker = attacker;
		this.ability = ability;
		this.damage = damage;
		this.element = element;
		this.sub = sub;
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

	/**
	 * 
	 * @return the element of the ability used
	 */
	public Element getElement() {
		return element;
	}
	
	/**
	 * 
	 * @return the subelement of the ability used
	 */
	public SubElement getSubElement() {
		return sub;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
