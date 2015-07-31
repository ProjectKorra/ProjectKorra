package com.projectkorra.ProjectKorra.Ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.configuration.ConfigLoadable;

/**
 * Represents an {@link ConfigLoadable} Ability.
 */
public abstract class CoreAbility implements Ability {

	/**
	 * ConcurrentHashMap that stores all Ability instances under UUID key.
	 * To access this hashmap use either {@link #getInstance()} from the
	 * ability instance or {@link #getInstance(StockAbilities)} from the
	 * outside.
	 */
	//private static ConcurrentHashMap<StockAbilities, ConcurrentHashMap<UUID, CoreAbility>> instances = new ConcurrentHashMap<>();
	//private static ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> instances = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Integer, CoreAbility> instances = new ConcurrentHashMap<>();
	//protected static AbilityMap<Ability> instances = new AbilityMap<>();
	private static ConcurrentHashMap<StockAbilities, ArrayList<Integer>> abilityMap = new ConcurrentHashMap<>(); 
	
	private static int ID = Integer.MIN_VALUE;
	private final StockAbilities stockAbility = getStockAbility();
	private final InstanceType type = getInstanceType();
	private Player player;
	private UUID uniqueId;
	private Integer id;
	
	/**
	 * Convenience method to check if a player already has an
	 * instance of this ability.
	 * 
	 * @param player The player to check
	 * @return true if instances contains the player
	 */
	public static final boolean containsPlayer(Player player, Class<? extends CoreAbility> ability) {
		List<CoreAbility> abilities = getAbilitiesFromPlayer(player);
		for (CoreAbility coreAbility : abilities) {
			if (ability.isInstance(coreAbility)) {
				if (coreAbility.getPlayer().getUniqueId().equals(player.getUniqueId())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets the list of ability instances that the player has created.
	 * 
	 * @param player The player to get
	 * @return list of abilities
	 */
	public static List<CoreAbility> getAbilitiesFromPlayer(Player player) {
		List<CoreAbility> abilities = new ArrayList<>();
		for (CoreAbility ability : instances.values()) {
			if (ability.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				abilities.add(ability);
			}
		}
		return abilities;
	}
	
	/**
	 * Gets the ability instance of the player.
	 * 
	 * @param player The player to get
	 * @param ability The ability class
	 * @return the ability instance or null
	 */
	public static CoreAbility getAbilityFromPlayer(Player player, Class<? extends CoreAbility> ability) {
		for (CoreAbility coreAbility : instances.values()) {
			if (ability.isInstance(coreAbility)) {
				if (coreAbility.getPlayer().getUniqueId().equals(player.getUniqueId())) {
					return coreAbility;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets the map with all the instances of CoreAbiliy's.
	 *  
	 * @return a map of core abilities
	 */
	public static ConcurrentHashMap<Integer, CoreAbility> getInstances() {
		return instances;
	}
	
	/**
	 * An access method to get an the instances of a {@link StockAbilities StockAbility}.
	 * 
	 * @param ability The instances map to get
	 * @return a map of instances from the specified {@link StockAbilities StockAbility}
	 */
	public final static ConcurrentHashMap<Integer, CoreAbility> getInstances(StockAbilities ability) {
		ConcurrentHashMap<Integer, CoreAbility> instanceMap = new ConcurrentHashMap<>();
		if (abilityMap.containsKey(ability)) {
			for (Integer id : abilityMap.get(ability)) {
				instanceMap.put(id, instances.get(id));
			}
		}
		return instanceMap;
	}
	
	public final static ConcurrentHashMap<Integer, CoreAbility> getInstances(Class<? extends CoreAbility> ability) {
		ConcurrentHashMap<Integer, CoreAbility> instanceMap = new ConcurrentHashMap<>();
		for (Integer id : instances.keySet()) {
			if (ability.isInstance(instances.get(id))) {
				instanceMap.put(id, instances.get(id));
			}
		}
		return instanceMap;
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances.
	 * 
	 * @see #progressAll(Class)
	 * @see #progressAll(StockAbilities)
	 */
	public static void progressAll() {
		for (Integer id : instances.keySet()) {
			instances.get(id).progress();
		}
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances
	 * of a specified ability.
	 * 
	 * @see #progressAll()
	 * @see #progressAll(StockAbilities)
	 */
	public static void progressAll(Class<? extends CoreAbility> ability) {
		for (Integer id : instances.keySet()) {
			if (ability.isInstance(instances.get(id))) {
				instances.get(id).progress();
			}
		}
	}
	
	/**
	 * Convenience method that calls {@link #progress()} for all instances
	 * of a specified stock ability.
	 * 
	 * @see #progressAll()
	 * @see #progressAll(Class) 
	 */
	public static void progressAll(StockAbilities ability) {
		for (Integer id : getInstances(ability).keySet()) {
			getInstances(ability).get(id).progress();
		}
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances.
	 * 
	 * @see #removeAll(StockAbilities)
	 * @see #removeAll(Class)
	 */
	public static void removeAll() {
		for (Integer id : instances.keySet()) {
			instances.get(id).remove();
		}
	}
	
	/**
	 * Convenience method that calls {@link #remove()} for all instances
	 * of a specified stock ability.
	 * 
	 * @see #removeAll()
	 * @see #removeAll(StockAbilities)
	 */
	public static void removeAll(Class<? extends CoreAbility> ability) {
		for (Integer id : instances.keySet()) {
			if (ability.isInstance(instances.get(id))) {
				instances.get(id).remove();
			}
		}
	}

	/**
	 * Convenience method that calls {@link #remove()} for all instances
	 * of a specified ability.
	 * 
	 * @see #removeAll()
	 * @see #removeAll(Class)
	 */
	public static void removeAll(StockAbilities ability) {
		for (Integer id : getInstances(ability).keySet()) {
			getInstances(ability).get(id).remove();
		}
	}
	
	/**
	 * Gets the id of the ability instance.
	 * 
	 * @return id of ability
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Convenience method to get instance for current ability class.
	 *  
	 * @return {@link #getInstance(StockAbilities)} for the current ability
	 */
	public CoreAbility getInstance() {
		return instances.get(id);
	}
	
	public InstanceType getInstanceType() {
		return InstanceType.SINGLE;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets the {@link StockAbilities StockAbility} that created this instance.
	 * This method will return null for abilities that are not stock abilities
	 * 
	 * @return stockabilities enum or null
	 */
	public abstract StockAbilities getStockAbility();

	/**
	 * Gets the {@link InstanceType} of the ability.
	 * 
	 * @return instance type
	 */
	public InstanceType getType() {
		return type;
	}
	
	public UUID getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * Put the instance of the ability into the instances map.
	 * 
	 * @param player The player
	 * @param ability The ability involved
	 */
	protected final void putInstance(Player player, CoreAbility ability) {
		this.id = ID;
		this.uniqueId = player.getUniqueId();
		this.player = player;
		instances.put(id, ability);
		if (stockAbility != null) {
			if (abilityMap.containsKey(stockAbility)) {
				abilityMap.get(stockAbility).add(id);
			} else {
				abilityMap.put(stockAbility, new ArrayList<Integer>(Arrays.asList(id)));
			}
		}
//		if (instances.containsKey(stockAbility)) {
//			if (type == InstanceType.MULTIPLE) {
//				instances.get(stockAbility).put(id, ability);
//			} else {
//				instances.get(stockAbility).put(uniqueId, ability);
//			}
//		} else {
//			ConcurrentHashMap<Object, CoreAbility> map = new ConcurrentHashMap<>();
//			if (type == InstanceType.MULTIPLE) {
//				map.put(id, ability);
//			} else {
//				map.put(uniqueId, ability);
//			}
//			instances.put(stockAbility, map);
//		}
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
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
	 * Removes the UUID from the instances map
	 */
	private final void removeInstance() {
//		if (instances.containsKey(stockAbility)) {
//			if (instances.get(stockAbility) != null) {
//				if (type == InstanceType.MULTIPLE) {
//					instances.get(getStockAbility()).remove(id);
//				} else {
//					instances.get(getStockAbility()).remove(uniqueId);
//				}
//			}
//		}
		if (instances.containsKey(id)) {
			instances.remove(id);
		}
		if (stockAbility != null) {
			if (abilityMap.containsKey(stockAbility)) {
				abilityMap.get(stockAbility).remove(id);
				if (abilityMap.get(stockAbility).isEmpty()) {
					abilityMap.remove(stockAbility);
				}
			}
		}
	}
}
