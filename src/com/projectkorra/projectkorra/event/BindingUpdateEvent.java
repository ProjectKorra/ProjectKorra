package com.projectkorra.projectkorra.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.ability.CoreAbility;

/**
 * Called when a player binds or unbinds an ability
 * 
 * @author savior67
 */
public class BindingUpdateEvent extends Event{
	
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private String ability;
	private int slot; //slot is -1 if it is a multiability
	private boolean isBinding; //true if the ability is being binded, otherwise false
	private boolean isMultiAbility; //true if the ability is a multiability
	private boolean cancelled;

	//bind event for abilities
	public BindingUpdateEvent(Player player, String ability, int slot, boolean isBinding) {
		this.player = player;
		this.ability = ability;
		this.slot = slot;
		this.isBinding = isBinding;
		this.cancelled = false;
		this.isMultiAbility = false;
	}
	
	//used for multi abilities
	public BindingUpdateEvent(Player player, String ability, boolean isBinding) {
		this.player = player;
		this.ability = ability;
		this.slot = -1;
		this.isBinding = isBinding;
		this.cancelled = false;
		this.isMultiAbility = true;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getAbility() {
		return ability;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public boolean isBinding() {
		return isBinding;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
