package com.projectkorra.projectkorra.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.VelocityChangeEvent;
import com.projectkorra.projectkorra.object.VelocityTracker;

public class VelocityBuilder {
	
	private static final Vector ZERO = new Vector(0, 0, 0);

	private Vector direction;
	private double knockback, knockup;
	
	public VelocityBuilder(Vector direction) {
		this.direction = direction.clone();
		this.knockback = 1;
		this.knockup = direction.getY();
	}
	
	public VelocityBuilder() {
		this(ZERO);
	}
	
	/**
	 * Sets the direction of the velocity to match the vector
	 */
	public VelocityBuilder direction(Vector direction) {
		this.direction = direction.clone();
		this.knockup = direction.getY();
		return this;
	}
	
	/**
	 * Sets the knockback (or speed) of the velocity to the given magnitude
	 */
	public VelocityBuilder knockback(double mag) {
		this.knockback = Math.abs(mag);
		return this;
	}
	
	/**
	 * Sets the knockup of the velocity to the given magnitude
	 * 
	 * <br><br>If the builder is calling {@link VelocityBuilder#direction(Vector)}, this method should be called after that.
	 */
	public VelocityBuilder knockup(double mag) {
		this.knockup = Math.abs(mag);
		return this;
	}
	
	public void apply(Entity entity, CoreAbility ability, boolean testImpact, boolean damageSelf, boolean movable) {
		if (direction.equals(ZERO)) {
			return;
		} else if (entity == null) {
			return;
		} else if (ability == null) {
			return;
		} else if (entity.hasMetadata("immovable") && entity.getMetadata("immovable").get(0).value() != ability) {
			return;
		}
		
		direction.setY(knockup).normalize().multiply(knockback);
		
		VelocityChangeEvent event = new VelocityChangeEvent(entity, ability, direction);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		if (!movable && !entity.hasMetadata("immovable")) {
			entity.setMetadata("immovable", new FixedMetadataValue(ProjectKorra.plugin, ability));
		}
		
		entity.setVelocity(direction);
		new VelocityTracker(entity, ability, testImpact, damageSelf);
	}
	
	public void apply(Entity entity, CoreAbility ability, boolean testImpact, boolean damageSelf) {
		apply(entity, ability, testImpact, damageSelf, true);
	}
	
	public void apply(Entity entity, CoreAbility ability) {
		apply(entity, ability, false, false, true);
	}
}
