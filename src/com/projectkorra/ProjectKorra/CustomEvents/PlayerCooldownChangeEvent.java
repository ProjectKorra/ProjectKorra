package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerCooldownChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String ability;
    private Result eventresult;
    private boolean cancelled;
    
    public PlayerCooldownChangeEvent(Player eplayer, String abilityname, Result result) {
        player = eplayer;
        ability = abilityname;
        eventresult = result;
        cancelled = false;
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
    
    public static enum Result {
        REMOVED, ADDED;
        private Result() {
        }
    }
    
}
