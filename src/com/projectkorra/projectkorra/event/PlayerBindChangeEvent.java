package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player binds or unbinds an ability
 */
public class PlayerBindChangeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean cancelled = false;
	
	private final Player player;
	private final String ability;
	private final int slot; 
	private final boolean isBinding, isMultiAbility; 

	public PlayerBindChangeEvent(Player player, String ability, int slot, boolean isBinding, boolean isMultiAbility) {
		this.player = player;
		this.ability = ability;
		this.slot = slot;
		this.isBinding = isBinding;
		this.isMultiAbility = isMultiAbility;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getAbility() {
		return this.ability;
	}

	/**
	 * Get the slot being changed. 
	 * <ul>
	 * <li>In the case of binding a multiability, returns 0
	 * <li>In the case of unbinding a multiability, returns the original slot
	 * </ul>
	 * @return affected slot
	 */
	public int getSlot() {
		return this.slot;
	}

	/**
	 * Get whether this event is binding or unbinding an ability
	 * @return true if binding, false if unbinding
	 */
	public boolean isBinding() {
		return this.isBinding;
	}

	/**
	 * Get whether this event was caused by a multiability or not
	 * @return true if caused by multiability
	 */
	public boolean isMultiAbility() {
		return this.isMultiAbility;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
