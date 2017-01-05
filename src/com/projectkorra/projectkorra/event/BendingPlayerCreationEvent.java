package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.BendingPlayer;
/**
 * Called when a new BendingPlayer is created
 */

public class BendingPlayerCreationEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private BendingPlayer bPlayer;
	
	public BendingPlayerCreationEvent(BendingPlayer bPlayer) {
		this.bPlayer = bPlayer;
	}

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
		return bPlayer;
	}
}

