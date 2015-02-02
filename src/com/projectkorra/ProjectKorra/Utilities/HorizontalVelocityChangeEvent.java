package com.projectkorra.ProjectKorra.Utilities;

import org.bukkit.entity.Entity;
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
	private Entity instigator;
	private Vector from;
	private Vector to;
	private Vector difference;

	public HorizontalVelocityChangeEvent(Entity entity, Entity instigator, Vector from, Vector to, Vector difference)
	{
		this.entity = entity;
		this.instigator = instigator;
		this.from = from;
		this.to = to;
		this.difference = difference;
	}

	public Entity getEntity()
	{
		return entity;
	}

	public Entity getInstigator()
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
