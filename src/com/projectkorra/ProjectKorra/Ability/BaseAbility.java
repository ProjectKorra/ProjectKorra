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
	private static ConcurrentHashMap<StockAbilities, ConcurrentHashMap<Object, BaseAbility>> instances = new ConcurrentHashMap<>();
	//protected static AbilityMap<Ability> instances = new AbilityMap<>();
	
	private static int ID = Integer.MIN_VALUE;
	private final StockAbilities stockAbility = getStockAbility();
	private final InstanceType type = getInstanceType();
	private Player player;
	private UUID uniqueId;
	private int id;
	
	protected final void putInstance(Player player, BaseAbility ability) {
		if (type == InstanceType.MULTIPLE) {
			this.id = ID;
		} else {
			this.id = -1;
		}
		this.uniqueId = player.getUniqueId();
		this.player = player;
		if (instances.containsKey(stockAbility)) {
			if (type == InstanceType.MULTIPLE) {
				instances.get(stockAbility).put(id, ability);
			} else {
				instances.get(stockAbility).put(uniqueId, ability);
			}
		} else {
			ConcurrentHashMap<Object, BaseAbility> map = new ConcurrentHashMap<>();
			if (type == InstanceType.MULTIPLE) {
				map.put(id, ability);
			} else {
				map.put(uniqueId, ability);
			}
			instances.put(stockAbility, map);
		}
		if (id != -1) {
			if (ID == Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			ID++;
		}
	}
	
	/**
	 * Removes the UUID from the instances map
	 */
	private final void removeInstance() {
		if (instances.containsKey(stockAbility)) {
			if (instances.get(stockAbility) != null) {
				if (type == InstanceType.MULTIPLE) {
					instances.get(getStockAbility()).remove(id);
				} else {
					instances.get(getStockAbility()).remove(uniqueId);
				}
			}
		}
	}
	
	/**
	 * An access method to get an the instances of a {@link StockAbilities StockAbility}.
	 * 
	 * @param abilty The instances map to get
	 * @return a map of instances from the specified {@link StockAbilities StockAbility}
	 */
	public final static ConcurrentHashMap<Object, BaseAbility> getInstance(StockAbilities ability) {
		if (instances.containsKey(ability)) {
			return instances.get(ability);
		}
		return new ConcurrentHashMap<Object, BaseAbility>();
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances
	 * of a specified ability.
	 * 
	 * @see #progressAll()
	 */
	public static void progressAll(StockAbilities ability) {
		for (Object object : getInstance(ability).keySet()) {
			instances.get(ability).get(object).progress();
		}
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances.
	 * 
	 * @see #progressAll(StockAbilities)
	 */
	public static void progressAll() {
		for (StockAbilities ability : instances.keySet()) {
			progressAll(ability);
		}
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances
	 * of a specified ability.
	 * 
	 * @see #removeAll()
	 */
	public static void removeAll(StockAbilities ability) {
		for (Object object : getInstance(ability).keySet()) {
			instances.get(ability).get(object).remove();
		}
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances.
	 * 
	 * @see #removeAll(StockAbilities)
	 */
	public static void removeAll() {
		for (StockAbilities ability : instances.keySet()) {
			removeAll(ability);
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
	public ConcurrentHashMap<Object, BaseAbility> getInstance() {
		return getInstance(stockAbility);
	}
	
	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}
	
	public InstanceType getInstanceType() {
		return InstanceType.SINGLE;
	}
	
	public int getID() {
		return id;
	}
}
