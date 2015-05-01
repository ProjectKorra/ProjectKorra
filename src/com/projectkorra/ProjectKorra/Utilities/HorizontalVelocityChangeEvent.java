package com.projectkorra.ProjectKorra.Utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityChangeEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    
    private boolean isCancelled;
    
    private Entity entity;
    private Player instigator;
    private Vector from;
    private Vector to;
    private Vector difference;
    private Location start;
    private Location end;
    
    @Deprecated
    public HorizontalVelocityChangeEvent(Entity entity, Player instigator, Vector from, Vector to, Vector difference)
    {
        this.entity = entity;
        this.instigator = instigator;
        this.from = from;
        this.to = to;
        this.difference = difference;
    }
    
    public HorizontalVelocityChangeEvent(Entity entity, Player instigator, Vector from, Vector to, Vector difference, Location start, Location end)
    {
        this.entity = entity;
        this.instigator = instigator;
        this.from = from;
        this.to = to;
        this.difference = difference;
        this.start = start;
        this.end = end;
    }
    
    public Entity getEntity()
    {
        return entity;
    }
    
    public Player getInstigator()
    {
        return instigator;
    }
    
    public Vector getFrom()
    {
        return from;
    }
    
    public Vector getTo()
    {
        return to;
    }
    
    public Location getStartPoint()
    {
        return start;
    }
    
    public Location getEndPoint()
    {
        return end;
    }
    
    public double getDistanceTraveled()
    {
        return start.distance(end);
    }
    
    public Vector getDifference()
    {
        return difference;
    }
    
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
    
    @Override
    public boolean isCancelled()
    {
        return isCancelled;
    }
    
    @Override
    public void setCancelled(boolean value)
    {
        this.isCancelled = value;
    }
}
