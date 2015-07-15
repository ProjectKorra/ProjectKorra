package com.projectkorra.ProjectKorra.Ability;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.configuration.ConfigLoadable;

/**
 * Represents an {@link ConfigLoadable} Ability.
 */
public abstract class BaseAbility implements Ability {

	/**
	 * ConcurrentHashMap that stores all Ability instances under UUID key.
	 * To access this hashmap use either {@link #getInstance()} from the
	 * ability instance or {@link #getInstance(StockAbilities)} from the
	 * outside.
	 */
	private static ConcurrentHashMap<StockAbilities, ConcurrentHashMap<UUID, BaseAbility>> instances = new ConcurrentHashMap<>();
	//protected static AbilityMap<Ability> instances = new AbilityMap<>();
	
	private final StockAbilities stockAbility = getStockAbility();
	private Player player;
	private UUID uniqueId;
	
	protected final void putInstance(Player player, BaseAbility ability) {
		this.uniqueId = player.getUniqueId();
		this.player = player;
		if (instances.containsKey(stockAbility)) {
			instances.get(stockAbility).put(uniqueId, ability);
		} else {
			ConcurrentHashMap<UUID, BaseAbility> map = new ConcurrentHashMap<>();
			map.put(uniqueId, ability);
			instances.put(stockAbility, map);
		}
	}
	
	/**
	 * Removes the UUID from the instances map
	 */
	private final void removeInstance() {
		if (instances.containsKey(stockAbility)) {
			if (instances.get(stockAbility) != null) {
				instances.get(getStockAbility()).remove(uniqueId);
			}
		}
	}
	
	/**
	 * An access method to get an the instances of a {@link StockAbilities StockAbility}.
	 * 
	 * @param abilty The instances map to get
	 * @return a map of instances from the specified {@link StockAbilities StockAbility}
	 */
	public final static ConcurrentHashMap<UUID, BaseAbility> getInstance(StockAbilities abilty) {
		if (instances.containsKey(abilty)) {
			return instances.get(abilty);
		}
		return new ConcurrentHashMap<UUID, BaseAbility>();
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances.
	 */
	public static void progressAll(StockAbilities ability) {
		for (UUID uuid : getInstance(ability).keySet()) {
			instances.get(ability).get(uuid).progress();
		}
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances.
	 */
	public static void removeAll(StockAbilities ability) {
		for (UUID uuid : getInstance(ability).keySet()) {
			instances.get(ability).get(uuid).remove();
		}
	}
	
	/**
	 * Calls {@link #removeInstance()}, Developers can override this
	 * method to do other things when remove is called but they 
	 * <strong>MUST</strong> remember to call {@code super.remove()}
	 * for the UUID to be properly removed from the {@link #instances}.
	 */
	@Override
	public void remove() {
		removeInstance();
	}
	
	/**
	 * Gets the {@link StockAbilities StockAbility} that created this instance.
	 * 
	 * @return stockabilities enum or null
	 */
	public abstract StockAbilities getStockAbility();
	
	/**
	 * Convenience method to get instance map for current ability class.
	 *  
	 * @return {@link #getInstance(StockAbilities)} for the current ability
	 */
	public ConcurrentHashMap<UUID, BaseAbility> getInstance() {
		return getInstance(stockAbility);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

}
