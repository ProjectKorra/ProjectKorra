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

	private final Player player;
	private final String ability;
	private final Result eventresult;
	private boolean cancelled;
	private long cooldown;

	public PlayerCooldownChangeEvent(final Player player, final String abilityname, final long cooldown, final Result result) {
		this.player = player;
		this.ability = abilityname;
		this.eventresult = result;
		this.cancelled = false;
		this.cooldown = cooldown;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getAbility() {
		return this.ability;
	}

	public Result getResult() {
		return this.eventresult;
	}

	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(final boolean cancel) {
		this.cancelled = cancel;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
