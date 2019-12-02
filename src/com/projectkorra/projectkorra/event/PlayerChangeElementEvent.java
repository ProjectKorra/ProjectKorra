package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.element.Element;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player's bending element is modified
 */
public class PlayerChangeElementEvent extends Event {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final Player player;
	private final Element element;
	private final Action action;

	/**
	 *
	 * @param player the {@link Player player} who's bending was changed
	 * @param element the {@link Element element} that was affected
	 * @param action whether the element was chosen, added, removed, or
	 *            permaremoved
	 */
	public PlayerChangeElementEvent(final Player player, final Element element, final Action action) {
		this.player = player;
		this.element = element;
		this.action = action;
	}

	/**
	 *
	 * @return the {@link Player player} who's bending was changed
	 */
	public Player getPlayer() {
		return this.player;
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
	public Action getAction() {
		return this.action;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

	public enum Action {
		ADD, SET, REMOVE, CLEAR
	}
}
