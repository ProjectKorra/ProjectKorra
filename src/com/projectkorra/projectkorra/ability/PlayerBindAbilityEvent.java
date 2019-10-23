package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerBindAbilityEvent extends PlayerEvent implements Cancellable
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final String abilityName;

	private boolean cancelled;
	private String cancelMessage;

	public PlayerBindAbilityEvent(Player player, String abilityName)
	{
		super(player);

		this.abilityName = abilityName;
	}

	public String getAbilityName()
	{
		return this.abilityName;
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

	public String getCancelMessage()
	{
		return this.cancelMessage;
	}

	public void setCancelMessage(String cancelMessage)
	{
		this.cancelMessage = cancelMessage;
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
}
