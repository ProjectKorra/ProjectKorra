package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.OfflineBendingPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a new BendingPlayer is created or loaded from the Database
 */

public class BendingPlayerLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineBendingPlayer bPlayer;

	public BendingPlayerLoadEvent(final OfflineBendingPlayer bPlayer) {
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
	public OfflineBendingPlayer getBendingPlayer() {
		return this.bPlayer;
	}

	/**
	 * Is the player who's bending was created online?
	 * @return true if the player is online
	 */
	public boolean isOnline() {
		return this.bPlayer.isOnline();
	}
}
