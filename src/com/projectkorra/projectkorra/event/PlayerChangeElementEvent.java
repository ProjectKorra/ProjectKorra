package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.Element;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player's bending element is modified
 */
public class PlayerChangeElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender;
	private Player target;
	private Element element;
	private Result result;

	/**
	 * 
	 * @param sender the {@link CommandSender} who changed the player's bending
	 * @param target the {@link Player player} who's bending was changed
	 * @param element the {@link Element element} that was affected
	 * @param result whether the element was chosen, added, removed, or permaremoved
	 */
	public PlayerChangeElementEvent(CommandSender sender, Player target, Element element, Result result) {
		this.sender = sender;
		this.target = target;
		this.element = element;
		this.result = result;
	}

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
		return sender;
	}

	/**
	 * 
	 * @return the {@link Player player} who's bending was changed
	 */
	public Player getTarget() {
		return target;
	}

	/**
	 * 
	 * @return the {@link Element element} that was affected
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * 
	 * @return whether the element was chosen, added, removed, or permaremoved
	 */
	public Result getResult() {
		return result;
	}

	public static enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE;
		private Result() {
		}
	}

}
