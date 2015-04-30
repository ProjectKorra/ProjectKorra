package com.projectkorra.ProjectKorra.CustomEvents;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerGrappleEvent extends Event implements Cancellable {
    
    public static final HandlerList handlers = new HandlerList();
    private Player player;
    private Entity entity;
    private Location pullLocation;
    private ItemStack hookItem;
    private boolean cancelled = false;
    
    public PlayerGrappleEvent(Player p, Entity e, Location l) {
        player = p;
        entity = e;
        pullLocation = l;
        hookItem = p.getItemInHand();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Entity getPulledEntity() {
        return entity;
    }
    
    public Location getPullLocation() {
        return pullLocation;
    }
    
    public ItemStack getHookItem() {
        return hookItem;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean set) {
        cancelled = set;
    }
    
}
