package com.projectkorra.ProjectKorra.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
 
public final class PlayerBendEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Element element;
    private String abilityname;
    private Location location;
    private boolean isSub;
    private String sub;
    private boolean cancelled;
 
    public PlayerBendEvent(Player eventplayer, Element eventelement, String eventability, Location eventlocation, Boolean isSubElement, String subelement) {
        player = eventplayer;
        element = eventelement;
        abilityname = eventability;
        location = eventlocation;
        isSub = isSubElement;
        sub = subelement;
    }
 
    public Player getPlayer() {
        return player;
    }
    
    public BendingPlayer getBPlayer() {
    	return Methods.getBendingPlayer(getPlayer().getName());
    }
    
    public Element getElement() {
    	return element;
    }
    
    public String getAbility() {
    	return abilityname;
    }
    
    public Location getLocation() {
    	return location;
    }
    
    public boolean isSubElement() {
    	return isSub;
    }
    
    public String getSubElement() {
    	if(isSubElement()) {
    		return sub;
    	}
    	return null;
    }
    
    public boolean isAvatarAbility() {
    	if(element == null) {
    		return true;
    	}
    	return false;
    }
 
    public boolean isCancelled() {
        return cancelled;
    }
 
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}