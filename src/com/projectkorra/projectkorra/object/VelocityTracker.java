package com.projectkorra.projectkorra.object;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.VelocityImpactDamageEvent;
import com.projectkorra.projectkorra.util.DamageHandler;

public class VelocityTracker extends BukkitRunnable {
	
	private static Map<Entity, VelocityTracker> trackers = new HashMap<>();
	
	private static final double DAMAGE_CAP = ConfigManager.getConfig().getDouble("Properties.CollisionPhysics.ImpactDamageCap");
	private static final double MINIMUM_DISTANCE = ConfigManager.getConfig().getDouble("Properties.CollisionPhysics.ImpactMinimumDistance");
	private static final boolean BARRIER_IMPACT = ConfigManager.getConfig().getBoolean("Properties.CollisionPhysics.ImpactOnBarrierBlock");
	private static final double VELOCITY_CHANGE = ConfigManager.getConfig().getDouble("Properties.CollisionPhysics.VelocityChangeThreshold");
	private static final double IMPACT_FACTOR = ConfigManager.getConfig().getDouble("Properties.CollisionPhysics.ImpactDamageFactor");

	private Entity entity;
	private CoreAbility ability;
	private Vector curr, prev;
	private long startTime;
	private Location start, last;
	private double travelled;
	private boolean testImpact, damageSelf;
	
	public VelocityTracker(Entity entity, CoreAbility ability, boolean testImpact, boolean damageSelf) {
		if (trackers.containsKey(entity)) {
			trackers.get(entity).cancel();
		}
		
		this.entity = entity;
		this.ability = ability;
		this.start = entity.getLocation();
		this.last = entity.getLocation();
		this.curr = entity.getVelocity().clone();
		this.prev = entity.getVelocity().clone();
		this.startTime = System.currentTimeMillis();
		this.testImpact = testImpact;
		this.damageSelf = damageSelf;
		this.travelled = 0;
		this.runTaskTimer(ProjectKorra.plugin, 1l, 1l);
		trackers.put(entity, this);
	}

	@Override
	public void run() {
		if (this.isCancelled()) {
			return;
		}
		
		if (ability.getPlayer() == null) {
			cancel();
			return;
		} else if (!damageSelf && ability.getPlayer().getEntityId() == entity.getEntityId()) {
			cancel();
			return;
		} else if (entity.isDead()) {
			cancel();
			return;
		} else if (entity.isOnGround()) {
			cancel();
			return;
		} else if (System.currentTimeMillis() > startTime + 4000) {
			cancel();
			return;
		}
		
		
		curr = entity.getVelocity();
		
		if (curr.angle(prev) > 100) {
			cancel();
			return;
		}
		
		travelled += last.distance(entity.getLocation());
		
		double diff = prev.length() - curr.length();
		
		prev = curr.clone();
		last = entity.getLocation();
		
		if (diff > VELOCITY_CHANGE && testImpact) {
			Vector direction = curr.clone().normalize();
			
			if (!BARRIER_IMPACT && entity.getLocation().add(direction).getBlock().getType() == Material.BARRIER) {
				cancel();
				return;
			} else if (entity.getLocation().getBlock().getType() == Material.WATER) {
				cancel();
				return;
			} else if (travelled < MINIMUM_DISTANCE) {
				cancel();
				return;
			} else if (curr.getY() < 0 && curr.angle(new Vector(0, -1, 0)) < 30) {
				return;
			}
			
			double damage = 0.5 + (IMPACT_FACTOR * ((start.distance(entity.getLocation()) - MINIMUM_DISTANCE) / diff));
			double capped = Math.min(damage, DAMAGE_CAP);
			
			VelocityImpactDamageEvent impact = new VelocityImpactDamageEvent(entity, ability, capped);
			Bukkit.getServer().getPluginManager().callEvent(impact);
			
			if (impact.isCancelled()) {
				cancel();
				return;
			}
			
			DamageHandler.damageEntity(entity, ability.getPlayer(), impact.getDamage(), ability, true);
			cancel();
			return;
		}
	}
	
	@Override
	public void cancel() {
		if (this.isCancelled()) {
			return;
		}
		
		super.cancel();
		trackers.remove(entity);
		if (entity.hasMetadata("immovable")) {
			entity.removeMetadata("immovable", ProjectKorra.plugin);
		}
	}
	
	public CoreAbility getAbility() {
		return ability;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public boolean canDamageSelf() {
		return damageSelf;
	}
	
	public double getDistanceTravelled() {
		return travelled;
	}
	
	public static void cancelAll() {
		for (VelocityTracker tracker : trackers.values()) {
			tracker.cancel();
		}
		trackers.clear();
	}
	
	public static VelocityTracker getTracker(Entity e) {
		if (trackers.containsKey(e)) {
			return trackers.get(e);
		}
		
		return null;
	}
}
