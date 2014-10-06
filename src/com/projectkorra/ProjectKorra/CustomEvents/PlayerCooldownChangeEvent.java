package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerCooldownChangeEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String ability;
    private Result eventresult;
    
    public PlayerCooldownChangeEvent(Player eplayer, String abilityname, Result result) {
        player = eplayer;
        ability = abilityname;
        eventresult = result;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public String getAbility() {
    	return ability;
    }
    
    public Result getResult() {
    	return eventresult;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
    public static enum Result {
    	REMOVED, ADDED;
    	private Result() {}
    }
	
	
	
}
