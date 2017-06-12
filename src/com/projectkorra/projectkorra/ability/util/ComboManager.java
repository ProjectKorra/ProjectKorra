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
import com.projectkorra.projectkorra.airbending.combo.AirStream;
import com.projectkorra.projectkorra.airbending.combo.AirSweep;
import com.projectkorra.projectkorra.airbending.combo.Twister;
import com.projectkorra.projectkorra.chiblocking.combo.Immobilize;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireKick;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireSpin;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireWheel;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.JetBlast;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.JetBlaze;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet.IceBulletLeftClick;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet.IceBulletRightClick;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;

public class ComboManager {

	private static final long CLEANUP_DELAY = 20 * 600;
	private static final Map<String, ArrayList<AbilityInformation>> RECENTLY_USED = new ConcurrentHashMap<>();
	private static final HashMap<String, ComboAbilityInfo> COMBO_ABILITIES = new HashMap<>();
	private static final HashMap<String, String> AUTHORS = new HashMap<>();
	private static final HashMap<String, String> DESCRIPTIONS = new HashMap<>();
	private static final HashMap<String, String> INSTRUCTIONS = new HashMap<>();

	public ComboManager() {
		COMBO_ABILITIES.clear();
		DESCRIPTIONS.clear();
		INSTRUCTIONS.clear();

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.FireCombo.FireKick.Enabled")) {
			ArrayList<AbilityInformation> fireKick = new ArrayList<>();
			fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
			fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
			fireKick.add(new AbilityInformation("FireBlast", ClickType.SHIFT_DOWN));
			fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("FireKick", new ComboAbilityInfo("FireKick", fireKick, FireKick.class));
			DESCRIPTIONS.put("FireKick", ConfigManager.languageConfig.get().getString("Abilities.Fire.Combo.FireKick.Description"));
			INSTRUCTIONS.put("FireKick", "FireBlast > FireBlast > (Hold Shift) > FireBlast.");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.FireCombo.FireSpin.Enabled")) {
			ArrayList<AbilityInformation> fireSpin = new ArrayList<>();
			fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
			fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
			fireSpin.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
			fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
			fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
			COMBO_ABILITIES.put("FireSpin", new ComboAbilityInfo("FireSpin", fireSpin, FireSpin.class));
			DESCRIPTIONS.put("FireSpin", ConfigManager.languageConfig.get().getString("Abilities.Fire.Combo.FireSpin.Description"));
			INSTRUCTIONS.put("FireSpin", "FireBlast > FireBlast > FireShield > (Tap Shift).");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.FireCombo.JetBlast.Enabled")) {
			ArrayList<AbilityInformation> jetBlast = new ArrayList<>();
			jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
			jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
			jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
			jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
			jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
			jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
			jetBlast.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("JetBlast", new ComboAbilityInfo("JetBlast", jetBlast, JetBlast.class));
			DESCRIPTIONS.put("JetBlast", ConfigManager.languageConfig.get().getString("Abilities.Fire.Combo.JetBlast.Description"));
			INSTRUCTIONS.put("JetBlast", "FireJet (Tap Shift) > FireJet (Tap Shift) > FireShield (Tap Shift) > FireJet.");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.FireCombo.JetBlaze.Enabled")) {
			ArrayList<AbilityInformation> jetBlaze = new ArrayList<>();
			jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
			jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
			jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
			jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
			jetBlaze.add(new AbilityInformation("Blaze", ClickType.SHIFT_DOWN));
			jetBlaze.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
			jetBlaze.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("JetBlaze", new ComboAbilityInfo("JetBlaze", jetBlaze, JetBlaze.class));
			DESCRIPTIONS.put("JetBlaze", ConfigManager.languageConfig.get().getString("Abilities.Fire.Combo.JetBlaze.Description"));
			INSTRUCTIONS.put("JetBlaze", "FireJet (Tap Shift) > FireJet (Tap Shift) > Blaze (Tap Shift) > FireJet.");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.FireCombo.FireWheel.Enabled")) {
			ArrayList<AbilityInformation> fireWheel = new ArrayList<>();
			fireWheel.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
			fireWheel.add(new AbilityInformation("FireShield", ClickType.RIGHT_CLICK_BLOCK));
			fireWheel.add(new AbilityInformation("FireShield", ClickType.RIGHT_CLICK_BLOCK));
			fireWheel.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
			COMBO_ABILITIES.put("FireWheel", new ComboAbilityInfo("FireWheel", fireWheel, FireWheel.class));
			DESCRIPTIONS.put("FireWheel", ConfigManager.languageConfig.get().getString("Abilities.Fire.Combo.FireWheel.Description"));
			INSTRUCTIONS.put("FireWheel", "FireShield (Hold Shift) > Right Click a block in front of you twice > Switch to Blaze > Release Shift.");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Air.AirCombo.Twister.Enabled")) {
			ArrayList<AbilityInformation> twister = new ArrayList<AbilityInformation>();
			twister.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
			twister.add(new AbilityInformation("AirShield", ClickType.SHIFT_UP));
			twister.add(new AbilityInformation("Tornado", ClickType.SHIFT_DOWN));
			twister.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("Twister", new ComboAbilityInfo("Twister", twister, Twister.class));
			DESCRIPTIONS.put("Twister", ConfigManager.languageConfig.get().getString("Abilities.Air.Combo.Twister.Description"));
			INSTRUCTIONS.put("Twister", "AirShield (Tap Shift) > Tornado (Hold Shift) > AirBlast (Left Click)");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Air.AirCombo.AirStream.Enabled")) {
			ArrayList<AbilityInformation> airStream = new ArrayList<>();
			airStream.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
			airStream.add(new AbilityInformation("AirSuction", ClickType.LEFT_CLICK));
			airStream.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("AirStream", new ComboAbilityInfo("AirStream", airStream, AirStream.class));
			DESCRIPTIONS.put("AirStream", ConfigManager.languageConfig.get().getString("Abilities.Air.Combo.AirStream.Description"));
			INSTRUCTIONS.put("AirStream", "AirShield (Hold Shift) > AirSuction (Left Click) > AirBlast (Left Click)");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Air.AirCombo.AirSweep.Enabled")) {
			ArrayList<AbilityInformation> airSweep = new ArrayList<>();
			airSweep.add(new AbilityInformation("AirSwipe", ClickType.LEFT_CLICK));
			airSweep.add(new AbilityInformation("AirSwipe", ClickType.LEFT_CLICK));
			airSweep.add(new AbilityInformation("AirBurst", ClickType.SHIFT_DOWN));
			airSweep.add(new AbilityInformation("AirBurst", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("AirSweep", new ComboAbilityInfo("AirSweep", airSweep, AirSweep.class));
			DESCRIPTIONS.put("AirSweep", ConfigManager.languageConfig.get().getString("Abilities.Air.Combo.AirSweep.Description"));
			INSTRUCTIONS.put("AirSweep", "AirSwipe (Left Click) > AirSwipe (Left Click) > AirBurst (Hold Shift) > AirBurst (Left Click)");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.WaterCombo.IceWave.Enabled")) {
			ArrayList<AbilityInformation> iceWave = new ArrayList<>();
			iceWave.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_UP));
			iceWave.add(new AbilityInformation("PhaseChange", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("IceWave", new ComboAbilityInfo("IceWave", iceWave, IceWave.class));
			DESCRIPTIONS.put("IceWave", ConfigManager.languageConfig.get().getString("Abilities.Water.Combo.IceWave.Description"));
			INSTRUCTIONS.put("IceWave", "Create a WaterSpout Wave > PhaseChange (Left Click)");
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.WaterCombo.IceBullet.Enabled")) {
			ArrayList<AbilityInformation> iceBullet = new ArrayList<>();
			iceBullet.add(new AbilityInformation("WaterBubble", ClickType.SHIFT_DOWN));
			iceBullet.add(new AbilityInformation("WaterBubble", ClickType.SHIFT_UP));
			iceBullet.add(new AbilityInformation("IceBlast", ClickType.SHIFT_DOWN));
			COMBO_ABILITIES.put("IceBullet", new ComboAbilityInfo("IceBullet", iceBullet, IceBullet.class));
			DESCRIPTIONS.put("IceBullet", ConfigManager.languageConfig.get().getString("Abilities.Water.Combo.IceBullet.Description"));
			INSTRUCTIONS.put("IceBullet", "WaterBubble (Tap Shift) > IceBlast (Hold Shift) > Wait for ice to Form > Then alternate between Left and Right click with IceBlast");

			ArrayList<AbilityInformation> iceBulletLeft = new ArrayList<>();
			iceBulletLeft.add(new AbilityInformation("IceBlast", ClickType.LEFT_CLICK));
			COMBO_ABILITIES.put("IceBulletLeftClick", new ComboAbilityInfo("IceBulletLeftClick", iceBulletLeft, IceBulletLeftClick.class));
			ArrayList<AbilityInformation> iceBulletRight = new ArrayList<>();
			iceBulletRight.add(new AbilityInformation("IceBlast", ClickType.RIGHT_CLICK_BLOCK));
			COMBO_ABILITIES.put("IceBulletRightClick", new ComboAbilityInfo("IceBulletRightClick", iceBulletRight, IceBulletRightClick.class));
		}

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Chi.ChiCombo.Immobilize.Enabled")) {
			ArrayList<AbilityInformation> immobilize = new ArrayList<>();
			immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
			immobilize.add(new AbilityInformation("SwiftKick", ClickType.LEFT_CLICK_ENTITY));
			immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
			immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
			COMBO_ABILITIES.put("Immobilize", new ComboAbilityInfo("Immobilize", immobilize, Immobilize.class));
			DESCRIPTIONS.put("Immobilize", ConfigManager.languageConfig.get().getString("Abilities.Chi.Combo.Immobilize.Description"));
			INSTRUCTIONS.put("Immobilize", "QuickStrike (Left Click) > SwiftKick (Left Click) > QuickStrike (Left Click) > QuickStrike (Left Click)");
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
					}
					catch (Exception e) {
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
