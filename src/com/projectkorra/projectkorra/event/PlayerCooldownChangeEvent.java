package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerCooldownChangeEvent extends Event implements Cancellable {
	
	public static enum Result {
		REMOVED, ADDED;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private Player player;
	private String ability;
	private Result eventresult;
	private boolean cancelled;
	private long cooldown;

	public PlayerCooldownChangeEvent(Player player, String abilityname, long cooldown, Result result) {
		this.player = player;
		this.ability = abilityname;
		this.eventresult = result;
		this.cancelled = false;
		this.cooldown = cooldown;
	}

	public Player getPlayer() {
		return player;
	}

	public String getAbility() {
		return ability;
	}

	public Result getResult() {
		return eventresult;
	}
	
	public long getCooldown() {
		return cooldown;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
