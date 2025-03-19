package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a new BendingPlayer is created or loaded from the Database.
 *
 * This event fires for loading both online AND offline players. Use
 * {@link #isOnline()} to check if the BendingPlayer object is available.
 *
 * If offline data was already cached and a player logs in, this event will fire
 * again but with {@link #isOnline()} returning true.
 */

public class BendingPlayerLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final OfflineBendingPlayer bPlayer;


	public BendingPlayerLoadEvent(final OfflineBendingPlayer bPlayer) {
		super(!Bukkit.isPrimaryThread());
		this.bPlayer = bPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * @return The OfflineBendingPlayer object that was loaded.
	 */
	public OfflineBendingPlayer getBendingPlayer() {
		return this.bPlayer;
	}

	/**
	 * Is the player who's bending data was loaded online? If this is true,
	 * it is safe to cast {@link #getBendingPlayer()} to {@link BendingPlayer}.
	 * @return true if the player is online
	 */
	public boolean isOnline() {
		return this.bPlayer.isOnline();
	}
}
