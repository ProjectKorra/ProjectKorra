package com.projectkorra.ProjectKorra.Ability;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.ProjectKorra.configuration.ConfigLoadable;

/**
 * Represents an {@link ConfigLoadable} Ability.
 */
public abstract class BaseAbility implements Ability {

	private static ConcurrentHashMap<StockAbilities, ConcurrentHashMap<UUID, BaseAbility>> instances = new ConcurrentHashMap<>();
	//protected static AbilityMap<Ability> instances = new AbilityMap<>();
	
	protected void putInstance(StockAbilities sb, UUID uuid, BaseAbility ability) {
		if (instances.containsKey(sb)) {
			instances.get(sb).put(uuid, ability);
		} else {
			ConcurrentHashMap<UUID, BaseAbility> map = new ConcurrentHashMap<>();
			map.put(uuid, ability);
			instances.put(sb, map);
		}
	}
	
	protected void removeInstance(StockAbilities sb, UUID uuid) {
		if (instances.containsKey(sb)) {
			if (instances.get(sb) != null) {
				instances.get(sb).remove(uuid);
			}
		}
	}
	
	public static ConcurrentHashMap<UUID, BaseAbility> getInstance(StockAbilities abilty) {
		if (instances.containsKey(abilty)) {
			return instances.get(abilty);
		}
		return new ConcurrentHashMap<UUID, BaseAbility>();
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances.
	 */
	public static void progressAll(StockAbilities ability) {
		for (UUID uuid : getInstance(ability).keySet())
			instances.get(ability).get(uuid).progress();
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances.
	 */
	public static void removeAll(StockAbilities ability) {
		for (UUID uuid : getInstance(ability).keySet()) {
			instances.get(ability).get(uuid).remove();
		}
	}

}
