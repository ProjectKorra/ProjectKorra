package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.ProjectKorra.Element;

public class PlayerChangeElementEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private CommandSender s;
	private Player t;
	private Element e;
	private Result r;
	
	public PlayerChangeElementEvent (CommandSender sender, Player target, Element element, Result result) {
		s = sender;
		t = target;
		e = element;
		r = result;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public CommandSender getSender() {
		return s;
	}
	
	public Player getTarget() {
		return t;
	}
	
	public Element getElement() {
		return e;
	}
	
	public Result getResult() {
		return r;
	}
	
	public static enum Result {
    	CHOOSE, REMOVE, ADD, PERMAREMOVE;
    	private Result() {}
    }

}
