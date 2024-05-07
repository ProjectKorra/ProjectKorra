package com.projectkorra.projectkorra.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the /bending reload command is executed.
 */

public class BendingReloadEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final CommandSender sender;

	public BendingReloadEvent(final CommandSender sender) {
		this.sender = sender;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * @return Who called the reload
	 */
	public CommandSender getSender() {
		return this.sender;
	}

	/**
	 * @return Whether the event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Sets if the event is cancelled
	 *
	 * @param cancel boolean value indicating whether the event is cancelled or
	 *            not
	 */
	@Override
	public void setCancelled(final boolean cancel) {
		this.cancelled = cancel;
	}
}
