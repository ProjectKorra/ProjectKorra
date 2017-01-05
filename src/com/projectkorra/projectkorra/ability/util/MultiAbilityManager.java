package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.event.BindChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiAbilityManager {

	public static Map<Player, HashMap<Integer, String>> playerAbilities = new ConcurrentHashMap<>();
	public static Map<Player, Integer> playerSlot = new ConcurrentHashMap<>();
	public static Map<Player, String> playerBoundAbility = new ConcurrentHashMap<>();
	public static ArrayList<MultiAbilityInfo> multiAbilityList = new ArrayList<MultiAbilityInfo>();

	public MultiAbilityManager() {
		ArrayList<MultiAbilityInfoSub> waterArms = new ArrayList<MultiAbilityInfoSub>();
		waterArms.add(new MultiAbilityInfoSub("Pull", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Punch", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grapple", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Grab", Element.WATER));
		waterArms.add(new MultiAbilityInfoSub("Freeze", Element.ICE));
		waterArms.add(new MultiAbilityInfoSub("Spear", Element.ICE));
		multiAbilityList.add(new MultiAbilityInfo("WaterArms", waterArms));
		manage();
	}

	/**
	 * Sets up a player's binds for a MultiAbility.
	 * 
	 * @param player Player having the multiability bound
	 * @param multiAbility MultiAbility being bound
	 */
	public static void bindMultiAbility(Player player, String multiAbility) {
		BindChangeEvent event = new BindChangeEvent(player, multiAbility, true);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(event.isCancelled())
			return;
		
		if (playerAbilities.containsKey(player))
			unbindMultiAbility(player);
		playerSlot.put(player, player.getInventory().getHeldItemSlot());
		playerBoundAbility.put(player, multiAbility);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		HashMap<Integer, String> currAbilities = new HashMap<Integer, String>();
		for (int i : bPlayer.getAbilities().keySet()) {
			currAbilities.put(i, bPlayer.getAbilities().get(i));
		}
		playerAbilities.put(player, currAbilities);

		List<MultiAbilityInfoSub> modes = getMultiAbility(multiAbility).getAbilities();

		bPlayer.getAbilities().clear();
		for (int i = 0; i < modes.size(); i++) {
			if (!player.hasPermission("bending.ability." + multiAbility + "." + modes.get(i).getName())) {
				bPlayer.getAbilities().put(i + 1, new StringBuilder().append(modes.get(i).getAbilityColor()).append(ChatColor.STRIKETHROUGH).append(modes.get(i).getName()).toString());
			} else {
				bPlayer.getAbilities().put(i + 1, modes.get(i).getAbilityColor() + modes.get(i).getName());
			}
		}

		if (player.isOnline()) {
			bPlayer.addCooldown("MAM_Setup", 1L); // Support for bending
			// scoreboards.
			player.getInventory().setHeldItemSlot(0);
		}
	}

	/**
	 * Returns the MultiAbility the player has bound. Returns null if no
	 * multiability is bound and active.
	 * 
	 * @param player The player to use
	 * @return name of multi ability bounded
	 */
	public static String getBoundMultiAbility(Player player) {
		if (playerBoundAbility.containsKey(player))
			return playerBoundAbility.get(player);
		return null;
	}

	/**
	 * Returns a MultiAbility based on name.
	 * 
	 * @param multiAbility Name of the multiability
	 * @return the multiability object or null
	 */
	public static MultiAbilityInfo getMultiAbility(String multiAbility) {
		for (MultiAbilityInfo ma : multiAbilityList) {
			if (ma.getName().equalsIgnoreCase(multiAbility))
				return ma;
		}
		return null;
	}

	/**
	 * Returns a boolean based on whether a player has a MultiAbility active.
	 * 
	 * @param player The player to check
	 * @return true If player has a multiability active
	 */
	public static boolean hasMultiAbilityBound(Player player) {
		if (playerAbilities.containsKey(player))
			return true;
		return false;
	}

	/**
	 * MultiAbility equivalent of {@link GeneralMethods#getBoundAbility(Player)}. Returns a
	 * boolean based on whether a player has a specific MultiAbility active.
	 * 
	 * @param player The player to check
	 * @param multiAbility The multiability name
	 * @return true If player has the specified multiability active
	 */
	public static boolean hasMultiAbilityBound(Player player, String multiAbility) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		
		if (playerAbilities.containsKey(player)) {
			if (!playerBoundAbility.get(player).equals(multiAbility) && bPlayer.getBoundAbility() != null)
				return false;
			return true;
		}
		return false;
	}

	public static void manage() {
		new BukkitRunnable() {
			public void run() {
				scrollHotBarSlots();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	/**
	 * Clears all MultiAbility data for a player. Called on player quit event.
	 * 
	 * @param player
	 */
	public static void remove(Player player) {
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
	public static void scrollHotBarSlots() {
		if (!playerAbilities.isEmpty()) {
			for (Player player : playerAbilities.keySet()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null) {
					continue;
				}
				if (playerBoundAbility.containsKey(player)) {
					if (bPlayer.getBoundAbility() == null) {
						if (multiAbilityList.contains(getMultiAbility(playerBoundAbility.get(player)))) {
							if (player.getInventory().getHeldItemSlot() >= getMultiAbility(playerBoundAbility.get(player)).getAbilities().size()) {
								player.getInventory().setHeldItemSlot(getMultiAbility(playerBoundAbility.get(player)).getAbilities().size() - 1);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Reverts a player's binds to a previous state before use of a
	 * MultiAbility.
	 * 
	 * @param player
	 */
	public static void unbindMultiAbility(Player player) {	
		if (playerAbilities.containsKey(player)) {
			HashMap<Integer, String> prevBinds = playerAbilities.get(player);
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return;
			}
			
			int lastNonNull = -1;
			for (int i = 1; i < 10; i++) {
				if (prevBinds.get(i) != null)
					lastNonNull = i;
				bPlayer.getAbilities().put(i, prevBinds.get(i));
			}
			if (lastNonNull > -1)
				GeneralMethods.saveAbility(bPlayer, lastNonNull, prevBinds.get(lastNonNull));

			if (player.isOnline())
				bPlayer.addCooldown("MAM_Setup", 1L); // Support for bending
			// scoreboards.
			playerAbilities.remove(player);
		}

		if (playerSlot.containsKey(player)) {
			if (player.isOnline())
				player.getInventory().setHeldItemSlot(playerSlot.get(player));
			playerSlot.remove(player);
		} else {
			if (player.isOnline())
				player.getInventory().setHeldItemSlot(0);
		}

		if (playerBoundAbility.containsKey(player))
			playerBoundAbility.remove(player);
	}

	/**
	 * MultiAbility class. Manages each MultiAbility's sub abilities.
	 *
	 */
	public static class MultiAbilityInfo {
		private String name;
		private ArrayList<MultiAbilityInfoSub> abilities;

		public MultiAbilityInfo(String name, ArrayList<MultiAbilityInfoSub> abilities) {
			this.name = name;
			this.abilities = abilities;
		}

		public ArrayList<MultiAbilityInfoSub> getAbilities() {
			return abilities;
		}

		public String getName() {
			return name;
		}

		public void setAbilities(ArrayList<MultiAbilityInfoSub> abilities) {
			this.abilities = abilities;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class MultiAbilityInfoSub {
		private String name;
		private Element element;

		public MultiAbilityInfoSub(String name, Element element) {
			this.name = name;
			this.element = element;
		}

		public Element getElement() {
			return element;
		}

		public String getName() {
			return name;
		}

		public void setElement(Element element) {
			this.element = element;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ChatColor getAbilityColor() {
			return element != null ? element.getColor() : null;
		}
	}

}
