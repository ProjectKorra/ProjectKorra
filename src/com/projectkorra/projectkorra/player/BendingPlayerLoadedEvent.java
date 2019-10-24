package com.projectkorra.projectkorra.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class BendingPlayerLoadedEvent extends PlayerEvent {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final BendingPlayer bendingPlayer;

	public BendingPlayerLoadedEvent(Player player, BendingPlayer bendingPlayer) {
		super(player);

		this.bendingPlayer = bendingPlayer;
	}

	public BendingPlayer getBendingPlayer() {
		return this.bendingPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
