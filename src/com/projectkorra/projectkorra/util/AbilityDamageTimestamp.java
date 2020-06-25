package com.projectkorra.projectkorra.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AbilityDamageTimestamp {
	
	private static final Map<Entity, AbilityDamageTimestamp> ENTITIES = new HashMap<>();
	
	private Ability ability;
	private long time;
	private boolean velocityDamage;
	private BukkitRunnable endTask;
	
	public AbilityDamageTimestamp(Entity entity, Ability ability, long time, boolean velocityDamage) {
		this.ability = ability;
		this.time = time;
		this.velocityDamage = velocityDamage;
		
		if (ENTITIES.containsKey(entity)) {
			ENTITIES.get(entity).endTask.cancel();
		}
		
		ENTITIES.put(entity, this);
		endTask = new BukkitRunnable() {

			@Override
			public void run() {
				if (isCancelled()) {
					return;
				}
				ENTITIES.remove(entity);
			}
			
		};
		endTask.runTaskLater(ProjectKorra.plugin, ConfigManager.getConfig().getLong("Properties.AbilityDamageTimestampDuration") / 1000 * 20);
	}
	
	public Ability getAbility() {
		return ability;
	}
	
	public long getTime() {
		return time;
	}
	
	public boolean isVelocityDamage() {
		return velocityDamage;
	}
	
	public static AbilityDamageTimestamp get(Entity entity) {
		if (ENTITIES.containsKey(entity)) {
			return ENTITIES.get(entity);
		}
		
		return null;
	}
}