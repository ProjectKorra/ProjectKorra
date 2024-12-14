package com.projectkorra.projectkorra.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;

public class PlayerChangeSubElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CommandSender sender;
	private final Player target;
	private final SubElement sub;
	private final Result result;

	/**
	 *
	 * @param sender the {@link CommandSender} who changed the player's bending
	 * @param target the {@link Player} who's bending was changed
	 * @param sub the {@link SubElement} that was changed to
	 * @param result whether the element was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeSubElementEvent(final CommandSender sender, final Player target, final SubElement sub, final Result result) {
		this.sender = sender;
		this.target = target;
		this.sub = sub;
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
	public SubElement getSubElement() {
		return this.sub;
	}

	/**
	 *
	 * @return whether the element was chosen, added, removed, or permaremoved
	 */
	public Result getResult() {
		return this.result;
	}

	public enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE,
		TEMP_ADD, TEMP_REMOVE, TEMP_EXPIRE;
	}
}
