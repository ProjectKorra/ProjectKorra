package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
/**
 * Called when the /bending reload command is executed.
 * 
 * @author kingbirdy
 * @version 1.0
 */

public class BendingReloadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public BendingReloadEvent() {

	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
