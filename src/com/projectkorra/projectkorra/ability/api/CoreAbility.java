package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.StockAbility;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an the core of all ProjectKorra abilities and implements the
 * {@link Ability} interface.
 * 
 * @author jacklin213
 * @version 1.0.0
 */
public abstract class CoreAbility implements Ability {

	/**
	 * ConcurrentHashMap that stores all Ability instances under UUID key. To
	 * access this hashmap use either {@link #getInstance()} from the ability
	 * instance or {@link #getInstance(StockAbility)} from the outside.
	 */
	//private static ConcurrentHashMap<StockAbility, ConcurrentHashMap<UUID, CoreAbility>> instances = new ConcurrentHashMap<>();
	//private static ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> instances = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Integer, CoreAbility> instances = new ConcurrentHashMap<>();
	//protected static AbilityMap<Ability> instances = new AbilityMap<>();
	private static ConcurrentHashMap<StockAbility, ArrayList<Integer>> abilityMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Class<? extends CoreAbility>, ConcurrentHashMap<Integer, CoreAbility>> classAbilityMap = new ConcurrentHashMap<>();

	private static int ID = Integer.MIN_VALUE;
	private final StockAbility stockAbility = getStockAbility();
	private Player player;
	private UUID uniqueId;
	private Integer id;

	/**
	 * Convenience method to check if a player already has an instance of this
	 * ability.
	 * 
	 * @param player The player to check
	 * @return true if instances contains the player
	 */
	public static final boolean containsPlayer(Player player, Class<? extends CoreAbility> ability) {
		CoreAbility coreAbility = getAbilityFromPlayer(player, ability);
		if (coreAbility != null) {
			return true;
		}
		/*
		 * List<CoreAbility> abilities = getAbilitiesFromPlayer(player); for
		 * (CoreAbility coreAbility : abilities) { if
		 * (ability.isInstance(coreAbility)) { if
		 * (coreAbility.getPlayer().getUniqueId().equals(player.getUniqueId()))
		 * { return true; } } }
		 */
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
	 * Gets the ability instance by its id.
	 * 
	 * @param id The ability id to get
	 * @return the ability instance or null
	 */
	public static final CoreAbility getAbility(int id) {
		return instances.get(id);
	}

	/**
	 * An access method to get an the instances of a {@link StockAbility}.
	 * <b>IMPORTANT: </b> If this is used in a for each loop use
	 * {@link #getAbility(int)} to get the ability. Incorrect usage may cause
	 * over looping and is capable of hanging the thead.
	 * 
	 * @param ability The instances map to get
	 * @return a map of instances from the specified {@link StockAbility}
	 * @see #getInstances(StockAbility)
	 */
	public final static ConcurrentHashMap<Integer, CoreAbility> getInstances(StockAbility ability) {
		ConcurrentHashMap<Integer, CoreAbility> instanceMap = new ConcurrentHashMap<>();
		if (abilityMap.containsKey(ability)) {
			for (Integer id : abilityMap.get(ability)) {
				instanceMap.put(id, instances.get(id));
			}
		}
		return instanceMap;
	}

	/**
	 * An access method to get an the instances of a {@link CoreAbility} by its
	 * class. <b>IMPORTANT: </b> If this is used in a for each loop use
	 * {@link #getAbility(int)} to get the ability. Incorrect usage may cause
	 * over looping and is capable of hanging the thead.
	 * 
	 * @param ability The instances map to get
	 * @return a map of instances from the specified class
	 * @see #getInstances(StockAbility)
	 */
	public final static ConcurrentHashMap<Integer, CoreAbility> getInstances(Class<? extends CoreAbility> ability) {
		ConcurrentHashMap<Integer, CoreAbility> instanceMap = classAbilityMap.get(ability.getClass());
		return instanceMap != null ? instanceMap : new ConcurrentHashMap<Integer, CoreAbility>();
	}

	//TODO: Update bending managers to use bellow method
	//	/**
	//	 * Convenience method that calls {@link #progress()} for all instances.
	//	 * 
	//	 * @see #progressAll(Class)
	//	 * @see #progressAll(StockAbility)
	//	 */
	//	public static void progressAll() {
	//		for (Integer id : instances.keySet()) {
	//			instances.get(id).progress();
	//		}
	//	}

	/**
	 * Convenience method that calls {@link #progress()} for all instances of a
	 * specified ability.
	 * 
	 * @see #progressAll(StockAbility)
	 */
	public static void progressAll(Class<? extends CoreAbility> ability) {
		for (Integer id : instances.keySet()) {
			if (ability.isInstance(instances.get(id))) {
				instances.get(id).progress();
			}
		}
	}

	/**
	 * Convenience method that calls {@link #progress()} for all instances of a
	 * specified stock ability.
	 *
	 * @see #progressAll(Class)
	 */
	public static void progressAll(StockAbility ability) {
		for (Integer id : getInstances(ability).keySet()) {
			getInstances(ability).get(id).progress();
		}
	}

	//TODO: Update bending managers to use bellow method
	//	/**
	//	 * Convenience method that calls {@link #remove()} for all instances.
	//	 * 
	//	 * @see #removeAll(StockAbility)
	//	 * @see #removeAll(Class)
	//	 */
	//	public static void removeAll() {
	//		for (Integer id : instances.keySet()) {
	//			instances.get(id).remove();
	//		}
	//	}

	/**
	 * Convenience method that calls {@link #remove()} for all instances of a
	 * specified stock ability.
	 * 
	 * @see #removeAll(StockAbility)
	 */
	public static void removeAll(Class<? extends CoreAbility> ability) {
		for (Integer id : instances.keySet()) {
			if (ability.isInstance(instances.get(id))) {
				instances.get(id).remove();
			}
		}
	}

	/**
	 * Convenience method that calls {@link #remove()} for all instances of a
	 * specified ability.
	 *
	 * @see #removeAll(Class)
	 */
	public static void removeAll(StockAbility ability) {
		for (Integer id : getInstances(ability).keySet()) {
			getInstances(ability).get(id).remove();
		}
	}

	/**
	 * Checks if ability is a {@link StockAbility} or not.
	 * 
	 * @return true if ability is a stock ability
	 */
	public boolean isStockAbility() {
		if (getStockAbility() == null) {
			return false;
		}
		return true;
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
	 * @return instance of the current ability
	 */
	public CoreAbility getInstance() {
		return instances.get(id);
	}

	/**
	 * Gets the {@link Ability.InstanceType} of the ability.
	 * 
	 * @return single by default
	 */
	public InstanceType getInstanceType() {
		return InstanceType.SINGLE;
	}

	/**
	 * Gets the player that invoked the ability.
	 * 
	 * @return player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the {@link StockAbility} that created this instance. This method
	 * will return null for abilities that are not stock abilities
	 * 
	 * @return StockAbility enum or null
	 */
	public abstract StockAbility getStockAbility();

	/**
	 * Gets the {@link UUID} of the player that invoked this ability.
	 * 
	 * @return the uuid of the player
	 */
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
		Class<? extends CoreAbility> classKey = ability.getClass();

		if (!classAbilityMap.containsKey(classKey)) {
			classAbilityMap.put(classKey, new ConcurrentHashMap<Integer, CoreAbility>());
		}
		classAbilityMap.get(classKey).put(id, ability);
		instances.put(id, ability);

		if (stockAbility != null) {
			if (abilityMap.containsKey(stockAbility)) {
				abilityMap.get(stockAbility).add(id);
			} else {
				abilityMap.put(stockAbility, new ArrayList<Integer>(Arrays.asList(id)));
			}
		}

		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	/**
	 * Calls {@link #removeInstance()}, Developers can override this method to
	 * do other things when remove is called but they <strong>MUST</strong>
	 * remember to call {@code super.remove()} for the ability to be properly
	 * removed from the {@link #instances}.
	 */
	@Override
	public void remove() {
		removeInstance();
	}

	/**
	 * Removes the ability instance from the instances map.
	 */
	private final void removeInstance() {
		if (instances.containsKey(id)) {
			instances.remove(id);
		}

		if (classAbilityMap.containsKey(this.getClass())) {
			classAbilityMap.get(this.getClass()).remove(id);
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
