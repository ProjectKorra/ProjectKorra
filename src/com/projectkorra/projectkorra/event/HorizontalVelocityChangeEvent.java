package com.projectkorra.projectkorra.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.Ability;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityChangeEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private boolean isCancelled;

	private final Entity entity;
	private final Player instigator;
	private final Vector from;
	private final Vector to;
	private final Vector difference;
	private Location start;
	private Location end;
	private Ability abil;

	@Deprecated
	public HorizontalVelocityChangeEvent(final Entity entity, final Player instigator, final Vector from, final Vector to, final Vector difference) {
		this.entity = entity;
		this.instigator = instigator;
		this.from = from;
		this.to = to;
		this.difference = difference;
	}

	public HorizontalVelocityChangeEvent(final Entity entity, final Player instigator, final Vector from, final Vector to, final Vector difference, final Location start, final Location end, final Ability ability) {
		this.entity = entity;
		this.instigator = instigator;
		this.from = from;
		this.to = to;
		this.difference = difference;
		this.start = start;
		this.end = end;
		this.abil = ability;
	}

	public Entity getEntity() {
		return this.entity;
	}

	public Player getInstigator() {
		return this.instigator;
	}

	public Vector getFrom() {
		return this.from;
	}

	public Vector getTo() {
		return this.to;
	}

	public Location getStartPoint() {
		return this.start;
	}

	public Location getEndPoint() {
		return this.end;
	}

	public double getDistanceTraveled() {
		if (!this.start.getWorld().equals(this.end.getWorld())) {
			return 0;
		}
		return this.start.distance(this.end);
	}

	public Vector getDifference() {
		return this.difference;
	}

	public Ability getAbility() {
		return this.abil;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(final boolean value) {
		this.isCancelled = value;
	}
}
