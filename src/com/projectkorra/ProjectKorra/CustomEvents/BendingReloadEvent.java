package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BendingReloadEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	public BendingReloadEvent () {

	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
