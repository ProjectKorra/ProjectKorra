package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Called when a player's bending element is modified
 */
public class PlayerChangeElementEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	@Nullable
	private final CommandSender sender;
	@NotNull
	private final OfflinePlayer target;
	@Nullable
	private final Element element;
	private final Result result;
	private boolean cancelled;

	/**
	 *
	 * @param sender the {@link CommandSender} who changed the player's bending
	 * @param target the {@link OfflinePlayer player} who's bending was changed
	 * @param element the {@link Element element} that was affected
	 * @param result whether the element was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeElementEvent(final CommandSender sender, final OfflinePlayer target, final Element element, final Result result) {
		super(!Bukkit.isPrimaryThread());
		this.sender = sender;
		this.target = target;
		this.element = element;
		this.result = result;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Get the sender responsible for changing the player's bending
	 * @return the {@link CommandSender} who changed the player's bending
	 */
	public @Nullable CommandSender getSender() {
		return this.sender;
	}

	/**
	 * Get the player who's bending was changed
	 * @return the {@link Player player} who's bending was changed
	 */
	public @NotNull OfflinePlayer getTarget() {
		return this.target;
	}

	/**
	 * Gets whether the result is a result where the element is removed. Including removing,
	 * permanently removing, temporary element removing, and temporary elements expiring.
	 * @return whether the element was removed
	 */
	public boolean isRemoved() {
		return this.result == Result.REMOVE || this.result == Result.PERMAREMOVE
				|| this.result == Result.TEMP_REMOVE || this.result == Result.TEMP_EXPIRE;
	}

	/**
	 * Is the target player online?
	 * @return whether the target player is online
	 */
	public boolean isTargetOnline() {
		return this.target.isOnline();
	}

	/**
	 * Gets whether the result is a result where the element changed is a temporary element
	 * @return whether the element being modified is temporary
	 */
	public boolean isTemporaryElement() {
		return this.result == Result.TEMP_ADD || this.result == Result.TEMP_REMOVE || this.result == Result.TEMP_EXPIRE;
	}

	/**
	 * Get the {@link BendingPlayer} that was affected
	 * @return the {@link BendingPlayer} that was affected
	 */
	public OfflineBendingPlayer getBendingPlayer() {
		return BendingPlayer.getBendingPlayer(this.target);
	}

	/**
	 *
	 * @return the {@link Element element} that was affected
	 */
	@Nullable
	public Element getElement() {
		return this.element;
	}

	/**
	 *
	 * @return whether the element was chosen, added, removed, or permaremoved
	 */
	public Result getResult() {
		return this.result;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE,
		TEMP_ADD, TEMP_REMOVE, TEMP_EXPIRE;
	}

}
