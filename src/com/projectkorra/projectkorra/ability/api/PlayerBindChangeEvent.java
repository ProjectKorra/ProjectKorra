package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerBindChangeEvent extends PlayerEvent implements Cancellable
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final String abilityName;
	private final int slot;
	private final Reason reason;

	private boolean cancelled;

	public PlayerBindChangeEvent(Player player, Reason reason) {
		this(player, null, -1, reason);
	}

	public PlayerBindChangeEvent(Player player, String abilityName, int slot, Reason reason)
	{
		super(player);

		this.abilityName = abilityName;
		this.slot = slot;
		this.reason = reason;
	}

	public String getAbilityName()
	{
		return this.abilityName;
	}

	public int getSlot() {
		return this.slot;
	}

	public Reason getReason() {
		return this.reason;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	public enum Reason {
		ADD, REMOVE, CLEAR
	}
}
