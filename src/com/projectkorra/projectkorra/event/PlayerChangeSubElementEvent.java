package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChangeSubElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender;
	private Player target;
	private SubElement sub;
	private Result result;

	/**
	 * 
	 * @param sender the {@link CommandSender} who changed the player's bending
	 * @param target the {@link Player} who's bending was changed
	 * @param sub the {@link SubElement} that was changed to
	 * @param result whether the element was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeSubElementEvent(CommandSender sender, Player target, SubElement sub, Result result) {
		this.sender = sender;
		this.target = target;
		this.sub = sub;
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
	public SubElement getSubElement() {
		return sub;
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
