package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerStanceChangeEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final String oldStance;
	private final String newStance;

	private boolean cancelled;

	public PlayerStanceChangeEvent(final Player player, final String oldStance, final String newStance) {
		super(player);
		this.oldStance = oldStance;
		this.newStance = newStance;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public String getOldStance() {
		return this.oldStance;
	}

	public String getNewStance() {
		return this.newStance;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
