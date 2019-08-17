package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.earth.EarthDomeConfig;
import com.projectkorra.projectkorra.earthbending.combo.EarthDomeOthers;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ReflectionHandler;

@SuppressWarnings("rawtypes")
public class ComboManager {
	private static final long CLEANUP_DELAY = 20 * 60;
	private static final Map<String, ArrayList<AbilityInformation>> RECENTLY_USED = new ConcurrentHashMap<>();
	private static final HashMap<String, ComboAbilityInfo> COMBO_ABILITIES = new HashMap<>();
	private static final HashMap<String, String> AUTHORS = new HashMap<>();
	private static final HashMap<String, String> DESCRIPTIONS = new HashMap<>();
	private static final HashMap<String, String> INSTRUCTIONS = new HashMap<>();

	public ComboManager() {
		COMBO_ABILITIES.clear();
		DESCRIPTIONS.clear();
		INSTRUCTIONS.clear();
		
		if (ConfigManager.getConfig(EarthDomeConfig.class).Enabled) {
			final ArrayList<AbilityInformation> earthDomeOthers = new ArrayList<>();
			earthDomeOthers.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
			earthDomeOthers.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("EarthDomeOthers", new ComboAbilityInfo("EarthDomeOthers", earthDomeOthers, EarthDomeOthers.class));
		}

		startCleanupTask();
	}

	public static void addComboAbility(final Player player, final ClickType type) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final String abilityName = bPlayer.getBoundAbilityName();
		if (abilityName == null) {
			return;
		}

		final AbilityInformation info = new AbilityInformation(abilityName, type, System.currentTimeMillis());
		addRecentAbility(player, info);

		final ComboAbilityInfo comboAbil = checkForValidCombo(player);
		if (comboAbil == null) {
			return;
		} else if (!player.hasPermission("bending.ability." + comboAbil.getName())) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (comboAbil.getComboType() instanceof Class) {
					final Class<?> clazz = (Class<?>) comboAbil.getComboType();
					try {
						ReflectionHandler.instantiateObject(clazz, player);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				} else {
					if (comboAbil.getComboType() instanceof ComboAbility) {
						((ComboAbility) comboAbil.getComboType()).createNewComboInstance(player);
						return;
					}
				}
			}

		}.runTaskLater(ProjectKorra.plugin, 1L);
	}

	/**
	 * Adds an {@link AbilityInformation} to the player's
	 * {@link ComboManager#RECENTLY_USED recentlyUsedAbilities}.
	 *
	 * @param player The player to add the AbilityInformation for
	 * @param info The AbilityInformation to add
	 */
	public static void addRecentAbility(final Player player, final AbilityInformation info) {
		ArrayList<AbilityInformation> list;
		final String name = player.getName();
		if (RECENTLY_USED.containsKey(name)) {
			list = RECENTLY_USED.get(name);
		} else {
			list = new ArrayList<AbilityInformation>();
		}

		list.add(info);
		RECENTLY_USED.put(name, list);
	}

	/**
	 * Checks if a Player's {@link ComboManager#RECENTLY_USED
	 * recentlyUsedAbilities} contains a valid set of moves to perform any
	 * combos. If it does, it returns the valid combo.
	 *
	 * @param player The player for whom to check if a valid combo has been
	 *            performed
	 * @return The ComboAbility of the combo that has been performed, or null if
	 *         no valid combo was found
	 */
	public static ComboAbilityInfo checkForValidCombo(final Player player) {
		final ArrayList<AbilityInformation> playerCombo = getRecentlyUsedAbilities(player, 8);
		for (final String ability : COMBO_ABILITIES.keySet()) {
			final ComboAbilityInfo customAbility = COMBO_ABILITIES.get(ability);
			final ArrayList<AbilityInformation> abilityCombo = customAbility.getAbilities();
			final int size = abilityCombo.size();

			if (playerCombo.size() < size) {
				continue;
			}

			boolean isValid = true;
			for (int i = 1; i <= size; i++) {
				final AbilityInformation playerInfo = playerCombo.get(playerCombo.size() - i);
				final AbilityInformation comboInfo = abilityCombo.get(abilityCombo.size() - i);
				if (playerInfo.getAbilityName().equals(comboInfo.getAbilityName()) && playerInfo.getClickType() == ClickType.LEFT_CLICK_ENTITY && comboInfo.getClickType() == ClickType.LEFT_CLICK) {
					continue;
				} else if (!playerInfo.equalsWithoutTime(comboInfo)) {
					isValid = false;
					break;
				}
			}

			if (isValid) {
				return customAbility;
			}
		}

		return null;
	}

	public static void cleanupOldCombos() {
		RECENTLY_USED.clear();
	}

	/**
	 * Gets the player's most recently used abilities, up to a maximum of 10.
	 *
	 * @param player The player to get recent abilities for
	 * @param amount The amount of recent abilities to get, starting from most
	 *            recent and getting older
	 * @return An ArrayList<{@link AbilityInformation}> of the player's recently
	 *         used abilities
	 */
	public static ArrayList<AbilityInformation> getRecentlyUsedAbilities(final Player player, final int amount) {
		final String name = player.getName();
		if (!RECENTLY_USED.containsKey(name)) {
			return new ArrayList<AbilityInformation>();
		}

		final ArrayList<AbilityInformation> list = RECENTLY_USED.get(name);
		if (list.size() < amount) {
			return new ArrayList<AbilityInformation>(list);
		}

		final ArrayList<AbilityInformation> tempList = new ArrayList<AbilityInformation>();
		for (int i = 0; i < amount; i++) {
			tempList.add(0, list.get(list.size() - 1 - i));
		}

		return tempList;
	}

	/**
	 * Gets all of the combos for a given element.
	 *
	 * @param element The element to get combos for
	 * @return An ArrayList of the combos for that element
	 */
	public static ArrayList<String> getCombosForElement(final Element element) {
		final ArrayList<String> list = new ArrayList<String>();
		for (final String comboab : COMBO_ABILITIES.keySet()) {
			final CoreAbility coreAbil = CoreAbility.getAbility(comboab);
			if (coreAbil == null) {
				continue;
			}

			Element abilElement = coreAbil.getElement();
			if (abilElement instanceof SubElement) {
				abilElement = ((SubElement) abilElement).getParentElement();
			}

			if (abilElement == element) {
				list.add(comboab);
			}
		}

		Collections.sort(list);
		return list;
	}

	public static void startCleanupTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
				cleanupOldCombos();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, CLEANUP_DELAY);
	}

	public static long getCleanupDelay() {
		return CLEANUP_DELAY;
	}

	public static HashMap<String, ComboAbilityInfo> getComboAbilities() {
		return COMBO_ABILITIES;
	}

	public static HashMap<String, String> getAuthors() {
		return AUTHORS;
	}

	public static HashMap<String, String> getDescriptions() {
		return DESCRIPTIONS;
	}

	public static HashMap<String, String> getInstructions() {
		return INSTRUCTIONS;
	}

	/**
	 * Contains information on an ability used in a combo.
	 *
	 * @author kingbirdy
	 *
	 */
	public static class AbilityInformation {
		private String abilityName;
		private ClickType clickType;
		private long time;

		public AbilityInformation(final String name, final ClickType type) {
			this(name, type, 0);
		}

		public AbilityInformation(final String name, final ClickType type, final long time) {
			this.abilityName = name;
			this.clickType = type;
			this.time = time;
		}

		/**
		 * Compares if two {@link AbilityInformation}'s are equal without
		 * respect to {@link AbilityInformation#time time}.
		 *
		 * @param info The AbilityInformation to compare against
		 * @return True if they are equal without respect to time
		 */
		public boolean equalsWithoutTime(final AbilityInformation info) {
			return this.getAbilityName().equals(info.getAbilityName()) && this.getClickType().equals(info.getClickType());
		}

		/**
		 * Gets the name of the ability.
		 *
		 * @return The name of the ability.
		 */
		public String getAbilityName() {
			return this.abilityName;
		}

		/**
		 * Gets the {@link ClickType} of the {@link AbilityInformation}.
		 *
		 * @return The ClickType
		 */
		public ClickType getClickType() {
			return this.clickType;
		}

		public long getTime() {
			return this.time;
		}

		public void setAbilityName(final String abilityName) {
			this.abilityName = abilityName;
		}

		public void setClickType(final ClickType clickType) {
			this.clickType = clickType;
		}

		public void setTime(final long time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return this.abilityName + " " + this.clickType + " " + this.time;
		}
	}

	public static class ComboAbilityInfo {
		private String name;
		private ArrayList<AbilityInformation> abilities;
		private Object comboType;

		public ComboAbilityInfo(final String name, final ArrayList<AbilityInformation> abilities, final Object comboType) {
			this.name = name;
			this.abilities = abilities;
			this.comboType = comboType;
		}

		public ArrayList<AbilityInformation> getAbilities() {
			return this.abilities;
		}

		public Object getComboType() {
			return this.comboType;
		}

		public String getName() {
			return this.name;
		}

		public void setAbilities(final ArrayList<AbilityInformation> abilities) {
			this.abilities = abilities;
		}

		public void setComboType(final Object comboType) {
			this.comboType = comboType;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
