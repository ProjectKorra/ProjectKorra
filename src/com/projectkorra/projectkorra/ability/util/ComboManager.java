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
import com.projectkorra.projectkorra.earthbending.combo.EarthDomeOthers;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet.IceBulletLeftClick;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet.IceBulletRightClick;

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

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.IceBullet.Enabled")) {
			ArrayList<AbilityInformation> iceBulletLeft = new ArrayList<>();
			iceBulletLeft.add(new AbilityInformation("IceBlast", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("IceBulletLeftClick", new ComboAbilityInfo("IceBulletLeftClick", iceBulletLeft, IceBulletLeftClick.class));
			ArrayList<AbilityInformation> iceBulletRight = new ArrayList<>();
			iceBulletRight.add(new AbilityInformation("IceBlast", ClickType.RIGHT_CLICK_BLOCK));
			COMBO_ABILITIES.put("IceBulletRightClick", new ComboAbilityInfo("IceBulletRightClick", iceBulletRight, IceBulletRightClick.class));
		}
		
		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Earth.EarthDome.Enabled")) {
			ArrayList<AbilityInformation> earthDomeOthers = new ArrayList<>();
			earthDomeOthers.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
			earthDomeOthers.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("EarthDomeOthers", new ComboAbilityInfo("EarthDomeOthers", earthDomeOthers, EarthDomeOthers.class));
		}

		startCleanupTask();
	}

	public static void addComboAbility(final Player player, ClickType type) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		String abilityName = bPlayer.getBoundAbilityName();
		if (abilityName == null) {
			return;
		}
		
		AbilityInformation info = new AbilityInformation(abilityName, type, System.currentTimeMillis());
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
					Class<?> clazz = (Class<?>) comboAbil.getComboType();
					try {
						ReflectionHandler.instantiateObject(clazz, player);
					} catch (Exception e) {
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
	public static void addRecentAbility(Player player, AbilityInformation info) {
		ArrayList<AbilityInformation> list;
		String name = player.getName();
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
	public static ComboAbilityInfo checkForValidCombo(Player player) {
		ArrayList<AbilityInformation> playerCombo = getRecentlyUsedAbilities(player, 8);
		for (String ability : COMBO_ABILITIES.keySet()) {
			ComboAbilityInfo customAbility = COMBO_ABILITIES.get(ability);
			ArrayList<AbilityInformation> abilityCombo = customAbility.getAbilities();
			int size = abilityCombo.size();

			if (playerCombo.size() < size) {
				continue;
			}

			boolean isValid = true;
			for (int i = 1; i <= size; i++) {
				AbilityInformation playerInfo = playerCombo.get(playerCombo.size() - i);
				AbilityInformation comboInfo = abilityCombo.get(abilityCombo.size() - i);
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
	public static ArrayList<AbilityInformation> getRecentlyUsedAbilities(Player player, int amount) {
		String name = player.getName();
		if (!RECENTLY_USED.containsKey(name)) {
			return new ArrayList<AbilityInformation>();
		}

		ArrayList<AbilityInformation> list = RECENTLY_USED.get(name);
		if (list.size() < amount) {
			return new ArrayList<AbilityInformation>(list);
		}

		ArrayList<AbilityInformation> tempList = new ArrayList<AbilityInformation>();
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
	public static ArrayList<String> getCombosForElement(Element element) {
		ArrayList<String> list = new ArrayList<String>();
		for (String comboab : COMBO_ABILITIES.keySet()) {
			CoreAbility coreAbil = CoreAbility.getAbility(comboab);
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

		public AbilityInformation(String name, ClickType type) {
			this(name, type, 0);
		}

		public AbilityInformation(String name, ClickType type, long time) {
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
		public boolean equalsWithoutTime(AbilityInformation info) {
			return this.getAbilityName().equals(info.getAbilityName()) && this.getClickType().equals(info.getClickType());
		}

		/**
		 * Gets the name of the ability.
		 * 
		 * @return The name of the ability.
		 */
		public String getAbilityName() {
			return abilityName;
		}

		/**
		 * Gets the {@link ClickType} of the {@link AbilityInformation}.
		 * 
		 * @return The ClickType
		 */
		public ClickType getClickType() {
			return clickType;
		}

		public long getTime() {
			return time;
		}

		public void setAbilityName(String abilityName) {
			this.abilityName = abilityName;
		}

		public void setClickType(ClickType clickType) {
			this.clickType = clickType;
		}

		public void setTime(long time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return abilityName + " " + clickType + " " + time;
		}
	}

	public static class ComboAbilityInfo {
		private String name;
		private ArrayList<AbilityInformation> abilities;
		private Object comboType;
		
		public ComboAbilityInfo(String name, ArrayList<AbilityInformation> abilities, Object comboType) {
			this.name = name;
			this.abilities = abilities;
			this.comboType = comboType;
		}

		public ArrayList<AbilityInformation> getAbilities() {
			return abilities;
		}

		public Object getComboType() {
			return comboType;
		}

		public String getName() {
			return name;
		}

		public void setAbilities(ArrayList<AbilityInformation> abilities) {
			this.abilities = abilities;
		}

		public void setComboType(Object comboType) {
			this.comboType = comboType;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}