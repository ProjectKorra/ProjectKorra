package com.projectkorra.ProjectKorra.CustomEvents;

import com.projectkorra.ProjectKorra.Element;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChangeElementEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender;
	private Player target;
	private Element element;
	private Result result;

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

	public CommandSender getSender() {
		return sender;
	}

	public Player getTarget() {
		return target;
	}

	public Element getElement() {
		return element;
	}

	public Result getResult() {
		return result;
	}

	public static enum Result {
		CHOOSE, REMOVE, ADD, PERMAREMOVE;
		private Result() {
		}
	}

}
