package com.projectkorra.ProjectKorra.Objects;

import com.projectkorra.projectkorra.Methods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Utilities.HorizontalVelocityChangeEvent;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
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
	private Player instigator;
	private Vector lastVelocity;
	private Vector thisVelocity;
	private Location launchLocation;
	private Location impactLocation;

	public HorizontalVelocityTracker(Entity e, Player instigator, long delay)
	{
		remove(e);
		entity = e;
		this.instigator = instigator;
		fireTime = System.currentTimeMillis();
		this.delay = delay;
		thisVelocity = e.getVelocity().clone();
		launchLocation = e.getLocation().clone();
		impactLocation = launchLocation.clone();
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

		List<Block> blocks = Methods.getBlocksAroundPoint(entity.getLocation(), 1.5);

		if(entity.isOnGround())
		{
			remove();
			return;
		}

		for(Block b : blocks)
		{
			if(Methods.isWater(b))
			{
				remove();
				return;
			}
		}

		if(thisVelocity.length() < lastVelocity.length())
		{
			if((diff.getX() > 1 || diff.getX() < -1)
					|| (diff.getZ() > 1 || diff.getZ() < -1))
			{
				impactLocation = entity.getLocation();
				for (Block b : blocks)
				{
					if (!Methods.isTransparentToEarthbending(instigator, b))
					{
						ProjectKorra.plugin.getServer().getPluginManager().callEvent(new HorizontalVelocityChangeEvent(entity, instigator, lastVelocity, thisVelocity, diff, launchLocation, impactLocation));
						remove();
						return;
					}
				}
			}
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
