package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModule;
import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModuleManager;

public class MultiAbilityManager {

	public static ConcurrentHashMap<Player, HashMap<Integer, String>> playerAbilities = new ConcurrentHashMap<Player, HashMap<Integer, String>>();
	public static ConcurrentHashMap<Player, Integer> playerSlot = new ConcurrentHashMap<Player, Integer>();
	public static ConcurrentHashMap<Player, String> playerBoundAbility = new ConcurrentHashMap<Player, String>();
	public static ArrayList<MultiAbility> multiAbilityList = new ArrayList<MultiAbility>();

	public MultiAbilityManager() {
		ArrayList<MultiAbilitySub> waterArms = new ArrayList<MultiAbilitySub>();
		waterArms.add(new MultiAbilitySub("Pull", Element.Water, null));
		waterArms.add(new MultiAbilitySub("Punch", Element.Water, null));
		waterArms.add(new MultiAbilitySub("Grapple", Element.Water, null));
		waterArms.add(new MultiAbilitySub("Grab", Element.Water, null));
		waterArms.add(new MultiAbilitySub("Freeze", Element.Water,
				SubElement.Icebending));
		waterArms.add(new MultiAbilitySub("Spear", Element.Water,
				SubElement.Icebending));
		multiAbilityList.add(new MultiAbility("WaterArms", waterArms));
		manage();
	}

	/**
	 * MultiAbility class. Manages each MultiAbility's sub abilities.
	 *
	 */
	public static class MultiAbility {
		private String name;
		private ArrayList<MultiAbilitySub> abilities;

		public MultiAbility(String name, ArrayList<MultiAbilitySub> abilities) {
			this.name = name;
			this.abilities = abilities;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<MultiAbilitySub> getAbilities() {
			return abilities;
		}

		public void setAbilities(ArrayList<MultiAbilitySub> abilities) {
			this.abilities = abilities;
		}
	}

	public static class MultiAbilitySub {
		private String name;
		private Element element;
		private SubElement sub;

		public MultiAbilitySub(String name, Element element, SubElement sub) {
			this.name = name;
			this.element = element;
			this.sub = sub;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Element getElement() {
			return element;
		}

		public void setElement(Element element) {
			this.element = element;
		}

		public SubElement getSubElement() {
			return sub;
		}

		public void setSubElement(SubElement sub) {
			this.sub = sub;
		}
	}

	/**
	 * Returns a MultiAbility based on name.
	 * 
	 * @param multiAbility
	 * @return
	 */
	public static MultiAbility getMultiAbility(String multiAbility) {
		for (MultiAbility ma : multiAbilityList) {
			if (ma.getName().equalsIgnoreCase(multiAbility))
				return ma;
		}
		return null;
	}

	/**
	 * Sets up a player's binds for a MultiAbility.
	 * 
	 * @param player
	 * @param multiAbility
	 */
	public static void bindMultiAbility(Player player, String multiAbility) {
		if (playerAbilities.containsKey(player))
			unbindMultiAbility(player);
		playerSlot.put(player, player.getInventory().getHeldItemSlot());
		playerBoundAbility.put(player, multiAbility);
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player
				.getName());
		HashMap<Integer, String> currAbilities = new HashMap<Integer, String>();
		for (int i : bPlayer.getAbilities().keySet()) {
			currAbilities.put(i, bPlayer.getAbilities().get(i));
		}
		playerAbilities.put(player, currAbilities);

		List<MultiAbilitySub> modes = getMultiAbility(multiAbility)
				.getAbilities();

		bPlayer.getAbilities().clear();
		for (int i = 0; i < modes.size(); i++) {
			if (!player.hasPermission("bending.ability." + multiAbility + "."
					+ modes.get(i).getName())) {
				bPlayer.getAbilities().put(
						i + 1,
						new StringBuilder()
								.append(GeneralMethods.getAbilityColor(modes.get(i).getName()))
								.append(ChatColor.STRIKETHROUGH)
								.append(modes.get(i).getName()).toString());
			} else {
				bPlayer.getAbilities()
						.put(i + 1,
								GeneralMethods.getAbilityColor(modes.get(i).getName())
										+ modes.get(i).getName());
			}
		}

		if (player.isOnline()) {
			bPlayer.addCooldown("MAM_Setup", 1L); // Support for bending
													// scoreboards.
			player.getInventory().setHeldItemSlot(0);
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
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player
					.getName());
			int lastNonNull = -1;
			for (int i = 1; i < 10; i++) {
				if (prevBinds.get(i) != null)
					lastNonNull = i;
				bPlayer.getAbilities().put(i, prevBinds.get(i));
			}
			if (lastNonNull > -1)
				GeneralMethods.saveAbility(bPlayer, lastNonNull,
						prevBinds.get(lastNonNull));

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
	 * MultiAbility equivalent of GeneralMethods.getBoundAbility(). Returns a
	 * boolean based on whether a player has a specific MultiAbility active.
	 * 
	 * @param player
	 * @param multiAbility
	 * @return
	 */
	public static boolean hasMultiAbilityBound(Player player,
			String multiAbility) {
		if (playerAbilities.containsKey(player)) {
			if (!playerBoundAbility.get(player).equals(multiAbility)
					&& GeneralMethods.getBoundAbility(player) != null)
				return false;
			return true;
		}
		return false;
	}

	/**
	 * Returns a boolean based on whether a player has a MultiAbility active.
	 * 
	 * @param player
	 * @return
	 */
	public static boolean hasMultiAbilityBound(Player player) {
		if (playerAbilities.containsKey(player))
			return true;
		return false;
	}

	/**
	 * Returns the MultiAbility the player has bound. Returns null if no
	 * multiability is bound and active.
	 * 
	 * @param player
	 * @return
	 */
	public static String getBoundMultiAbility(Player player) {
		if (playerBoundAbility.containsKey(player))
			return playerBoundAbility.get(player);
		return null;
	}

	public static void manage() {
		new BukkitRunnable() {
			public void run() {
				scrollHotBarSlots();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	/**
	 * Keeps track of the player's selected slot while a MultiAbility is active.
	 */
	public static void scrollHotBarSlots() {
		if (!playerAbilities.isEmpty()) {
			for (Player player : playerAbilities.keySet()) {
				if (playerBoundAbility.containsKey(player)) {
					if (GeneralMethods.getBoundAbility(player) == null) {
						if (multiAbilityList
								.contains(getMultiAbility(playerBoundAbility
										.get(player)))) {
							if (player.getInventory().getHeldItemSlot() > getMultiAbility(
									playerBoundAbility.get(player))
									.getAbilities().size()) {
								player.getInventory().setHeldItemSlot(
										getMultiAbility(
												playerBoundAbility.get(player))
												.getAbilities().size() - 1);
							} else {
								player.getInventory().setHeldItemSlot(0);
							}
						}
					}
				}
			}
		}
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
		List<MultiAbilityModule> abilities = MultiAbilityModuleManager.multiAbility;
		for (MultiAbilityModule mam : abilities)
			mam.stop();

		playerAbilities.clear();
		playerSlot.clear();
		playerBoundAbility.clear();
	}
}
