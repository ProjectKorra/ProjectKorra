package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBendingDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Player victim;
	private Player attacker;
	private String ability;
	private double damage;

	public PlayerBendingDeathEvent(Player victim, Player attacker, String ability, double damage) {
		this.victim = victim;
		this.attacker = attacker;
		this.ability = ability;
		this.damage = damage;
	}

	public Player getVictim() {
		return victim;
	}

	public Player getAttacker() {
		return attacker;
	}

	public String getAbility() {
		return ability;
	}

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
