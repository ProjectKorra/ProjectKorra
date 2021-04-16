package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJumpEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final double height;

	public PlayerJumpEvent(final Player player, final double height) {
		this.player = player;
		this.height = height;
	}

	public Player getPlayer() {
		return this.player;
	}

	public double getHeight() {
		return this.height;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
