package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a new BendingPlayer is created
 */

public class BendingPlayerCreationEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final BendingPlayer bPlayer;

	public BendingPlayerCreationEvent(final BendingPlayer bPlayer) {
		this.bPlayer = bPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * @return BendingPlayer created
	 */
	public BendingPlayer getBendingPlayer() {
		return this.bPlayer;
	}
}
