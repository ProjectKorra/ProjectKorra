package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityDamageByBendingEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private Player attacker;
	private Entity target;
	private String ability;
	private double damage;
	private boolean cancelled = false;
	
	public EntityDamageByBendingEvent(Player Attacker, Entity Target, String Ability, double Damage) {
		attacker = Attacker;
		target = Target;
		ability = Ability;
		damage = Damage;
	}
	
	public Player getAttacker() {
		return attacker;
	}
	
	public Entity getTarget() {
		return target;
	}
	
	public String getAbilityName() {
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
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean set) {
		cancelled = set;
	}
}
