package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a player's scoreboard for bending purposes
 */
public class BendingBoardInstance {
	private final String[] cachedSlots = new String[10];
	private final Set<String> misc = new HashSet<>(); // Stores scoreboard scores for combos and misc abilities

	private final Player player;
	private final BendingPlayer bendingPlayer;

	private final Scoreboard bendingBoard;
	private final Objective bendingSlots;
	private int selectedSlot;

	public BendingBoardInstance(final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		player = bPlayer.getPlayer();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		String title = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.Title"));
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", "dummy", title);
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(bendingBoard);

		Arrays.fill(cachedSlots, "");
		updateAll();
	}

	public void disableScoreboard() {
		bendingBoard.clearSlot(DisplaySlot.SIDEBAR);
		bendingSlots.unregister();
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	private void setSlot(int slot, String name, boolean cooldown) {
		if (slot < 1 || slot > 9 || !player.getScoreboard().equals(bendingBoard)) return;
		String prefix = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.SelectionPrefix"));
		StringBuilder sb = new StringBuilder(slot == selectedSlot ? prefix : String.join("", Collections.nCopies(ChatColor.stripColor(prefix).length(), " ")));
		if (name == null || name.isEmpty()) {
			String emptySlot = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.EmptySlot").replaceAll("\\{slot_number\\}", "" + slot));
			sb.append(emptySlot);
		} else {
			CoreAbility coreAbility = CoreAbility.getAbility(ChatColor.stripColor(name));
			if (coreAbility == null) { // MultiAbility
				if (cooldown || bendingPlayer.isOnCooldown(name)) sb.append(ChatColor.STRIKETHROUGH);
				sb.append(name);
			} else {
				sb.append(coreAbility.getMovePreviewWithoutCooldownTimer(player, cooldown));
			}
		}
		sb.append(ChatColor.values()[slot].toString()); // Unique suffix

		if (!cachedSlots[slot].equals(sb.toString())) {
			bendingBoard.resetScores(cachedSlots[slot]);
		}
		cachedSlots[slot] = sb.toString();
		bendingSlots.getScore(sb.toString()).setScore(-slot);
	}

	public void updateAll() {
		final Map<Integer, String> boundAbilities = new HashMap<>(bendingPlayer.getAbilities());
		for (int i = 1; i <= 9; i++) {
			setSlot(i, boundAbilities.getOrDefault(i, ""), false);
		}
	}

	public void clearSlot(int slot) {
		setSlot(slot, null, false);
	}

	public void setActiveSlot(int oldSlot, int newSlot) {
		if (selectedSlot != oldSlot) {
			oldSlot = selectedSlot; // Fixes bug when slot is set using setHeldItemSlot
		}
		selectedSlot = newSlot;
		setSlot(oldSlot, bendingPlayer.getAbilities().getOrDefault(oldSlot, ""), false);
		setSlot(newSlot, bendingPlayer.getAbilities().getOrDefault(newSlot, ""), false);
	}

	public void setAbility(String name, boolean cooldown) {
		final Map<Integer, String> boundAbilities = bendingPlayer.getAbilities();
		boundAbilities.keySet().stream().filter(key -> name.equals(boundAbilities.get(key))).forEach(slot -> setSlot(slot, name, cooldown));
	}

	public void updateMisc(String text, boolean show, boolean isCombo) {
		String selection = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.SelectionPrefix"));
		String prefix = String.join("", Collections.nCopies(ChatColor.stripColor(selection).length(), " "));
		
		String miscSeparator = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.MiscSeparator"));
		String alignedText = prefix + text;
		
		if (show) {
			if (misc.isEmpty()) {
				bendingSlots.getScore(miscSeparator).setScore(-10);
			}

			misc.add(alignedText);
			bendingSlots.getScore(alignedText).setScore(isCombo ? -11 : -12);
		} else {
			misc.remove(alignedText);
			bendingBoard.resetScores(alignedText);

			if (misc.isEmpty()) {
				bendingBoard.resetScores(miscSeparator);
			}
		}
	}
}
