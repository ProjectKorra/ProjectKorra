package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;

public class MultiAbilityManager {

	public static Map<Player, HashMap<Integer, String>> playerAbilities = new ConcurrentHashMap<>();
	public static Map<Player, Integer> playerSlot = new ConcurrentHashMap<>();
	public static Map<Player, String> playerBoundAbility = new ConcurrentHashMap<>();
	public static ArrayList<MultiAbilityInfo> multiAbilityList = new ArrayList<MultiAbilityInfo>();

	public MultiAbilityManager() {
		final ArrayList<MultiAbilityInfoSub> waterArms = new ArrayList<MultiAbilityInfoSub>();
		waterArms.add(new MultiAbilityInfoSub("Pull", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Punch", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grapple", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grab", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Freeze", Element.ICE));
		waterArms.add(new MultiAbilityInfoSub("Spear", Element.ICE));
		multiAbilityList.add(new MultiAbilityInfo("WaterArms", waterArms));
	}

	/**
	 * Sets up a player's binds for a MultiAbility.
	 *
	 * @param player Player having the multiability bound
	 * @param multiAbility MultiAbility being bound
	 */
	public static void bindMultiAbility(final Player player, final String multiAbility) {
		if (!player.isOnline()) {
			return;
		}
		
		final PlayerBindChangeEvent event = new PlayerBindChangeEvent(player, multiAbility, 0, true, true);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		if (playerAbilities.containsKey(player)) {
			unbindMultiAbility(player);
		}
		
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		playerSlot.put(player, player.getInventory().getHeldItemSlot());
		playerBoundAbility.put(player, multiAbility);
		playerAbilities.put(player, new HashMap<Integer, String>(bPlayer.getAbilities()));

		final List<MultiAbilityInfoSub> modes = getMultiAbility(multiAbility).getAbilities();

		bPlayer.getAbilities().clear();
		for (int i = 0; i < modes.size(); i++) {
			if (!player.hasPermission("bending.ability." + multiAbility + "." + modes.get(i).getName())) {
				bPlayer.getAbilities().put(i + 1, new StringBuilder().append(modes.get(i).getAbilityColor()).append(ChatColor.STRIKETHROUGH).append(modes.get(i).getName()).toString());
			} else {
				bPlayer.getAbilities().put(i + 1, modes.get(i).getAbilityColor() + modes.get(i).getName());
			}
		}
		
		player.getInventory().setHeldItemSlot(0);
	}

	/**
	 * Returns the MultiAbility the player has bound. Returns null if no
	 * multiability is bound and active.
	 *
	 * @param player The player to use
	 * @return name of multi ability bounded
	 */
	public static String getBoundMultiAbility(final Player player) {
		if (playerBoundAbility.containsKey(player)) {
			return playerBoundAbility.get(player);
		}
		return null;
	}

	/**
	 * Returns a MultiAbility based on name.
	 *
	 * @param multiAbility Name of the multiability
	 * @return the multiability object or null
	 */
	public static MultiAbilityInfo getMultiAbility(final String multiAbility) {
		for (final MultiAbilityInfo ma : multiAbilityList) {
			if (ma.getName().equalsIgnoreCase(multiAbility)) {
				return ma;
			}
		}
		return null;
	}

	/**
	 * Returns a boolean based on whether a player has a MultiAbility active.
	 *
	 * @param player The player to check
	 * @return true If player has a multiability active
	 */
	public static boolean hasMultiAbilityBound(final Player player) {
		if (playerAbilities.containsKey(player)) {
			return true;
		}
		return false;
	}

	/**
	 * MultiAbility equivalent of
	 * {@link GeneralMethods#getBoundAbility(Player)}. Returns a boolean based
	 * on whether a player has a specific MultiAbility active.
	 *
	 * @param player The player to check
	 * @param multiAbility The multiability name
	 * @return true If player has the specified multiability active
	 */
	public static boolean hasMultiAbilityBound(final Player player, final String multiAbility) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		return playerAbilities.containsKey(player) && playerBoundAbility.get(player).equals(multiAbility);
	}

	/**
	 * Clears all MultiAbility data for a player. Called on player quit event.
	 *
	 * @param player
	 */
	public static void remove(final Player player) {
		playerAbilities.remove(player);
		playerBoundAbility.remove(player);
		playerSlot.remove(player);
	}

	/**
	 * Cleans up all MultiAbilities.
	 */
	public static void removeAll() {
		playerAbilities.clear();
		playerSlot.clear();
		playerBoundAbility.clear();
	}

	/**
	 * Keeps track of the player's selected slot while a MultiAbility is active.
	 */
	public static boolean canChangeSlot(final Player player, int slot) {
		if (playerAbilities.isEmpty()) {
			return true;
		}
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null) {
			if (bPlayer.getBoundAbility() == null && multiAbilityList.contains(getMultiAbility(playerBoundAbility.getOrDefault(player, "")))) {
				return slot < getMultiAbility(playerBoundAbility.get(player)).getAbilities().size();
			}
		}
		return true;
	}

	/**
	 * Reverts a player's binds to a previous state before use of a
	 * MultiAbility.
	 *
	 * @param player
	 */
	public static void unbindMultiAbility(final Player player) {
		if (!player.isOnline()) {
			return;
		}
		
		playerAbilities.compute(player, MultiAbilityManager::resetBinds);
		playerBoundAbility.remove(player);
		playerSlot.remove(player);
	}
	
	private static HashMap<Integer, String> resetBinds(Player player, HashMap<Integer, String> prevBinds) {
		if (prevBinds == null) {
			return null;
		}
		
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}
		
		player.getInventory().setHeldItemSlot(playerSlot.getOrDefault(player, 0));
		ProjectKorra.plugin.getServer().getPluginManager().callEvent(new PlayerBindChangeEvent(player, playerBoundAbility.get(player), false, true));

		for (int i = 1; i < 10; i++) {
			bPlayer.getAbilities().put(i, prevBinds.get(i));
		}
		
		return null;
	}

	/**
	 * MultiAbility class. Manages each MultiAbility's sub abilities.
	 *
	 */
	public static class MultiAbilityInfo {
		private String name;
		private ArrayList<MultiAbilityInfoSub> abilities;

		public MultiAbilityInfo(final String name, final ArrayList<MultiAbilityInfoSub> abilities) {
			this.name = name;
			this.abilities = abilities;
		}

		public ArrayList<MultiAbilityInfoSub> getAbilities() {
			return this.abilities;
		}

		public String getName() {
			return this.name;
		}

		public void setAbilities(final ArrayList<MultiAbilityInfoSub> abilities) {
			this.abilities = abilities;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

	public static class MultiAbilityInfoSub {
		private String name;
		private Element element;

		public MultiAbilityInfoSub(final String name, final Element element) {
			this.name = name;
			this.element = element;
		}

		public Element getElement() {
			return this.element;
		}

		public String getName() {
			return this.name;
		}

		public void setElement(final Element element) {
			this.element = element;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public ChatColor getAbilityColor() {
			return this.element != null ? this.element.getColor() : null;
		}
	}

}
