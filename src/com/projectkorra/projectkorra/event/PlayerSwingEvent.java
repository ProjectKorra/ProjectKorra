package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerSwingEvent extends PlayerEvent {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final BendingPlayer bendingPlayer;
	private final String abilityName;

	public PlayerSwingEvent(Player player, BendingPlayer bendingPlayer, String abilityName) {
		super(player);

		this.bendingPlayer = bendingPlayer;
		this.abilityName = abilityName;
	}

	public BendingPlayer getBendingPlayer() {
		return this.bendingPlayer;
	}

	public String getAbilityName() {
		return this.abilityName;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
