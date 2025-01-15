package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStanceChangeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String oldStance;
	private final String newStance;

	private boolean cancelled;

	public PlayerStanceChangeEvent(final Player player, final String oldStance, final String newStance) {
		this.player = player;
		this.oldStance = oldStance;
		this.newStance = newStance;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getOldStance() {
		return this.oldStance;
	}

	public String getNewStance() {
		return this.newStance;
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
