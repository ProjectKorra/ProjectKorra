package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.CustomEvents.PlayerCooldownChangeEvent;
import com.projectkorra.ProjectKorra.CustomEvents.PlayerCooldownChangeEvent.Result;

/**
 * Class that presents a player and stores all bending information
 * about the player.
 */
public class BendingPlayer {

	/**
	 * ConcurrentHashMap that contains all instances of BendingPlayer, with UUID key.
	 */
	private static ConcurrentHashMap<UUID, BendingPlayer> players = new ConcurrentHashMap<>();
	//	public static ConcurrentHashMap<String, Long> blockedChi = new ConcurrentHashMap<String, Long>();

	private UUID uuid;
	private String name;
	private ArrayList<Element> elements;
	private HashMap<Integer, String> abilities;
	private ConcurrentHashMap<String, Long> cooldowns;
	private boolean permaRemoved;
	private boolean toggled = true;
	private long slowTime = 0;
	private boolean tremorSense = true;
	private boolean chiBlocked = false;

	/**
	 * Creates a new {@link BendingPlayer}.
	 * 
	 * @param uuid The unique identifier
	 * @param playerName The playername
	 * @param elements The known elements
	 * @param abilities The known abilities
	 * @param permaRemoved The permanent removed status
	 */
	public BendingPlayer(UUID uuid, String playerName, ArrayList<Element> elements, HashMap<Integer, String> abilities, boolean permaRemoved) {
		this.uuid = uuid;
		this.name = playerName;
		this.elements = elements;
		this.setAbilities(abilities);
		this.permaRemoved = permaRemoved;
		cooldowns = new ConcurrentHashMap<String, Long>();

		players.put(uuid, this);
		PKListener.login(this);
	}

	/**
	 * Gets the map of {@link BendingPlayers}.
	 * 
	 * @return {@link #players}
	 */
	public static ConcurrentHashMap<UUID, BendingPlayer> getPlayers() {
		return players;
	}

	/**
	 * Adds an ability to the cooldowns map while firing a
	 * {@link PlayerCooldownChangeEvent}.
	 * 
	 * @param ability Name of the ability
	 * @param cooldown The cooldown time
	 */
	public void addCooldown(String ability, long cooldown) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.put(ability, cooldown + System.currentTimeMillis());
		}
	}

	/**
	 * Adds an element to the {@link BendingPlayer}'s known list.
	 * 
	 * @param e The element to add
	 */
	public void addElement(Element e) {
		this.elements.add(e);
	}

	/**
	 * Sets chiBlocked to true.
	 */
	public void blockChi() {
		chiBlocked = true;
	}

	/**
	 * Checks to see if {@link BendingPlayer} can be slowed.
	 * 
	 * @return true If player can be slowed
	 */
	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	/**
	 * Gets the map of abilities that the {@link BendingPlayer} knows.
	 * 
	 * @return map of abilities
	 */
	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}
	
	/**
	 * Gets the cooldown time of the ability.
	 * 
	 * @param ability The ability to check
	 * @return the cooldown time 
	 * <p>
	 * or -1 if cooldown doesn't exist
	 * </p>
	 */
	public long getCooldown(String ability) {
		if (cooldowns.containsKey(ability)) {
			return cooldowns.get(ability);
		}
		return -1;
	}

	/**
	 * Gets the map of cooldowns of the {@link BendingPlayer}.
	 * 
	 * @return map of cooldowns
	 */
	public ConcurrentHashMap<String, Long> getCooldowns() {
		return cooldowns;
	}

	/**
	 * Gets the list of elements the {@link BendingPlayer} knows.
	 * 
	 * @return a list of elements
	 */
	public List<Element> getElements() {
		return this.elements;
	}

	/**
	 * Gets the name of the {@link BendingPlayer}.
	 * 
	 * @return the player name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets the unique identifier of the {@link BendingPlayer}.
	 * 
	 * @return the uuid
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * Convenience method to {@link #getUUID()} as a string.
	 * 
	 * @return string version of uuid
	 */
	public String getUUIDString() {
		return this.uuid.toString();
	}

	/**
	 * Checks to see if the {@link BendingPlayer} knows a specific element.
	 * 
	 * @param e The element to check
	 * @return true If the player knows the element
	 */
	public boolean hasElement(Element e) {
		return this.elements.contains(e);
	} 

	/**
	 * Checks to see if the {@link BendingPlayer} is chi blocked.
	 * 
	 * @return true If the player is chi blocked
	 */
	public boolean isChiBlocked() {
		return this.chiBlocked;
	}

	/**
	 * Checks to see if a specific ability is on cooldown.
	 * 
	 * @param ability The ability name to check
	 * @return true if the cooldown map contains the ability
	 */
	public boolean isOnCooldown(String ability) {
		return this.cooldowns.containsKey(ability);
	}

	/**
	 * Checks if the {@link BendingPlayer} is permaremoved.
	 * 
	 * @return true If the player is permaremoved
	 */
	public boolean isPermaRemoved() {
		return this.permaRemoved;
	}

	/**
	 * Checks if the {@link BendingPlayer} has bending toggled on.
	 * 
	 * @return true If bending is toggled on
	 */
	public boolean isToggled() {
		return this.toggled;
	}

	/**
	 * Checks if the {@link BendingPlayer} is tremor sensing.
	 * 
	 * @return true if player is tremor sensing
	 */
	public boolean isTremorSensing() {
		return this.tremorSense;
	}

	/**
	 * Removes the cooldown of an ability.
	 * 
	 * @param ability The ability's cooldown to remove
	 */
	public void removeCooldown(String ability) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			this.cooldowns.remove(ability);
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s abilities. This method also
	 * saves the abilities to the database.
	 * 
	 * @param abilities The abilities to set/save
	 */
	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + (abilities.get(i) == null ? null: abilities.get(i)) + "' WHERE uuid = '" + uuid + "'");
		}
	}
	
	/**
	 * Sets the {@link BendingPlayer}'s element.
	 * If the player had elements before they will be overwritten.
	 * 
	 * @param e The element to set
	 */
	public void setElement(Element e) {
		this.elements.clear();
		this.elements.add(e);
	}

	/**
	 * Sets the permanent removed state of the {@link BendingPlayer}.
	 * @param permaRemoved
	 */
	public void setPermaRemoved(boolean permaRemoved) {
		this.permaRemoved = permaRemoved;
	}
	
	/**
	 * Slow the {@link BendingPlayer} for a certain amount of time.
	 * 
	 * @param cooldown The amount of time to slow.
	 */
	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}
	
	/**
	 * Toggles the {@link BendingPlayer}'s bending.
	 */
	public void toggleBending() {
		toggled = !toggled;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s tremor sensing.
	 */
	public void toggleTremorSense() {
		tremorSense = !tremorSense;
	}
	
	/**
	 * Sets the {@link BendingPlayer}'s chi blocked to false.
	 */
	public void unblockChi() {
		chiBlocked = false;
	}
	
}
