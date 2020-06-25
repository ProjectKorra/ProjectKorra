package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.CoreAbility;

public class VelocityImpactDamageEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();

	private Entity entity;
	private CoreAbility ability;
	private double damage;
	private boolean cancelled;
	
	public VelocityImpactDamageEvent(Entity entity, CoreAbility ability, double damage) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
		this.cancelled = false;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public CoreAbility getAbility() {
		return ability;
	}
	
	public double getDamage() {
		return damage;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
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
