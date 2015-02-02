package com.projectkorra.ProjectKorra.Objects;

import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.HorizontalVelocityChangeEvent;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityTracker
{
	public static ConcurrentHashMap<Entity, HorizontalVelocityTracker> instances = new ConcurrentHashMap<Entity, HorizontalVelocityTracker>();

	private long delay;
	private long fireTime;
	private Entity entity;
	private Entity instigator;
	private Vector lastVelocity;
	private Vector thisVelocity;

	public HorizontalVelocityTracker(Entity e, Entity instigator, long delay)
	{
		entity = e;
		this.instigator = instigator;
		fireTime = System.currentTimeMillis();
		this.delay = delay;
		thisVelocity = e.getVelocity();
		this.delay = delay;
		update();
		instances.put(entity, this);
	}

	public void update()
	{
		if(System.currentTimeMillis() < fireTime + delay)
			return;

		lastVelocity = thisVelocity.clone();
		thisVelocity = entity.getVelocity().clone();

		Vector diff = thisVelocity.subtract(lastVelocity);

		if(entity.isOnGround())
			remove();

		if((diff.getX() > 1 || diff.getX() < -1)
				|| (diff.getZ() > 1 || diff.getZ() < -1))
		{
			ProjectKorra.plugin.getServer().getPluginManager().callEvent(new HorizontalVelocityChangeEvent(entity, instigator, lastVelocity, thisVelocity, diff));
			remove();
			return;
		}
	}

	public static void updateAll()
	{
		for(Entity e : instances.keySet())
			instances.get(e).update();
	}

	public void remove()
	{
		instances.remove(entity);
	}

	public static void remove(Entity e)
	{
		if(instances.containsKey(e))
			instances.remove(e);
	}
}
