package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages every individual {@link BendingBoardInstance}
 */
public class BendingBoardManager {
	private static final Set<String> disabledWorlds = new HashSet<>();
	private static final Set<UUID> disabledPlayers = new HashSet<>();
	private static final Map<Player, BendingBoardInstance> scoreboardPlayers = new ConcurrentHashMap<>();

	private static boolean enabled;

	public static void setup() {
		enabled = ConfigManager.boardConfig.get().getBoolean("Enable");
		disabledPlayers.clear();
		scoreboardPlayers.clear();
		disabledWorlds.addAll(ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds"));
		for (String s : ConfigManager.boardConfig.get().getStringList("DisabledPlayers")) {
			disabledPlayers.add(UUID.fromString(s));
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			canUseScoreboard(player);
		}
	}

	public static void reload() {
		for (BendingBoardInstance pBoard : scoreboardPlayers.values()) {
			pBoard.disableScoreboard();
		}
		setup();
	}

	/**
	 * Force toggle the scoreboard for when a player changes worlds (for example when teleporting to a world where bending is disabled)
	 * @param player
	 */
	public static void forceToggleScoreboard(Player player) {
		if (disabledWorlds.contains(player.getWorld().getName())) {
			if (scoreboardPlayers.containsKey(player)) {
				scoreboardPlayers.get(player).disableScoreboard();
				scoreboardPlayers.remove(player);
			}
		} else {
			canUseScoreboard(player);
		}
	}

	public static void toggleScoreboard(Player player) {
		if (!enabled || disabledWorlds.contains(player.getWorld().getName())) {
			GeneralMethods.sendBrandingMessage(player, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Board.Disabled"));
			return;
		}

		if (scoreboardPlayers.containsKey(player)) {
			scoreboardPlayers.get(player).disableScoreboard();
			disabledPlayers.add(player.getUniqueId());
			scoreboardPlayers.remove(player);
			GeneralMethods.sendBrandingMessage(player, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Board.ToggledOff"));
		} else {
			disabledPlayers.remove(player.getUniqueId());
			canUseScoreboard(player);
			GeneralMethods.sendBrandingMessage(player, ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Board.ToggledOn"));
		}
	}

	/**
	 * Checks if a player can use the bending board and creates a BendingBoardInstance if possible.
	 * @param player the player to check
	 * @return true if player can use the bending board, false otherwise
	 */
	public static boolean canUseScoreboard(Player player) {
		if (!enabled || disabledPlayers.contains(player.getUniqueId()) || disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		}
		if (!scoreboardPlayers.containsKey(player)) {
			scoreboardPlayers.put(player, new BendingBoardInstance(player));
		}
		return true;
	}

	public static void updateAllSlots(Player player) {
		if (canUseScoreboard(player)) {
			scoreboardPlayers.get(player).updateAll();
		}
	}

	public static void updateBoard(Player player, String abilityName, boolean cooldown, int slot) {
		if (canUseScoreboard(player)) {
			if (abilityName == null || abilityName.isEmpty()) {
				scoreboardPlayers.get(player).clearSlot(slot);
				return;
			}
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				scoreboardPlayers.get(player).updateAll();
				return;
			}
			CoreAbility coreAbility = CoreAbility.getAbility(abilityName);
			if (coreAbility != null && ComboManager.getComboAbilities().containsKey(abilityName)) {
				scoreboardPlayers.get(player).updateCombo("  " + coreAbility.getElement().getColor() + ChatColor.STRIKETHROUGH + abilityName, cooldown);
				return;
			}
			scoreboardPlayers.get(player).setAbility(abilityName, cooldown);
		}
	}

	public static void changeActiveSlot(Player player, int oldSlot, int newSlot) {
		if (canUseScoreboard(player)) {
			scoreboardPlayers.get(player).setActiveSlot(++oldSlot, ++newSlot);
		}
	}

	public static void saveChanges() {
		ConfigManager.boardConfig.get().set("DisabledPlayers", disabledPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
		ConfigManager.boardConfig.save();
	}
}
