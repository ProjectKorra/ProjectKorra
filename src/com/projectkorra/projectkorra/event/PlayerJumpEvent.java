package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJumpEvent extends Event{
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private Player player;
	private double height;
	
	public PlayerJumpEvent(Player player, double height) {
		this.player = player;
		this.height = height;
	}

	public Player getPlayer() {
		return player;
	}
	
	public double getHeight() {
		return height;
	}
	
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

}
