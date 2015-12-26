package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;

public final class PlayerCooldownChangeEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private String ability;
	private Result eventresult;
	private boolean cancelled;
	private BendingPlayer bPlayer;
	private long cooldown;

	public PlayerCooldownChangeEvent(Player player, String abilityname, Result result) {
		this.player = player;
		this.ability = abilityname;
		this.eventresult = result;
		this.cancelled = false;
		this.bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		this.cooldown = bPlayer.getCooldown(ability);
	}

	public Player getPlayer() {
		return player;
	}

	public String getAbility() {
		return ability;
	}

	public Result getResult() {
		return eventresult;
	}
	
	public long getCooldown() {
		return cooldown;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public static enum Result {
		REMOVED, ADDED;
		private Result() {
		}
	}

}
