package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player binds or unbinds an ability
 *
 * @author savior67
 */
public class PlayerBindChangeEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final String ability;
	private final int slot; // slot is -1 if it is a multiability.
	private final boolean isBinding; // true if the ability is being binded, otherwise false.
	private final boolean isMultiAbility; // true if the ability is a multiability.
	private boolean cancelled;

	// bind event for abilities.
	public PlayerBindChangeEvent(final Player player, final String ability, final int slot, final boolean isBinding) {
		this.player = player;
		this.ability = ability;
		this.slot = slot;
		this.isBinding = isBinding;
		this.cancelled = false;
		this.isMultiAbility = false;
	}

	// used for multi abilities.
	public PlayerBindChangeEvent(final Player player, final String ability, final boolean isBinding) {
		this.player = player;
		this.ability = ability;
		this.slot = -1;
		this.isBinding = isBinding;
		this.cancelled = false;
		this.isMultiAbility = true;
	}

	public String getAbility() {
		return this.ability;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getSlot() {
		return this.slot;
	}

	public boolean isBinding() {
		return this.isBinding;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public boolean isMultiAbility() {
		return this.isMultiAbility;
	}

	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}
}
