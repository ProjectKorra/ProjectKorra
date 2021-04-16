package com.projectkorra.projectkorra.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.Element;

/**
 * Called when a player's bending element is modified
 */
public class PlayerChangeElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CommandSender sender;
	private final Player target;
	private final Element element;
	private final Result result;

	/**
	 *
	 * @param sender the {@link CommandSender} who changed the player's bending
	 * @param target the {@link Player player} who's bending was changed
	 * @param element the {@link Element element} that was affected
	 * @param result whether the element was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeElementEvent(final CommandSender sender, final Player target, final Element element, final Result result) {
		this.sender = sender;
		this.target = target;
		this.element = element;
		this.result = result;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 *
	 * @return the {@link CommandSender} who changed the player's bending
	 */
	public CommandSender getSender() {
		return this.sender;
	}

	/**
	 *
	 * @return the {@link Player player} who's bending was changed
	 */
	public Player getTarget() {
		return this.target;
	}

	/**
	 *
	 * @return the {@link Element element} that was affected
	 */
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

	public static enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE;
		private Result() {}
	}

}
