package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BendingDamageEvent extends Event implements Cancellable {
	
	public static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player attacker;
	private Entity damaged;
	private double damage;
	private String ability;
	
	public BendingDamageEvent(Player attacker, Entity damaged, double damage, String ability) {
		this.cancelled = false;
		this.attacker = attacker;
		this.damaged = damaged;
		this.damage = damage;
		this.ability = ability;
		
	}
	
	public String getAbility() {
		return ability;
	}
	
	public void setAbility(String ability) {
		this.ability = ability;
	}
	
	public double getDamage() {
		return damage;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	public Entity getDamaged() {
		return damaged;
	}
	
	public Player getAttacker() {
		return attacker;
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
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

}
