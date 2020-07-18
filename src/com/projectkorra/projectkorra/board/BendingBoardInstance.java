package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

/**
 * Represents a player's scoreboard for bending purposes
 */
public class BendingBoardInstance {
	private final String[] cachedSlots = new String[10];
	private final Set<String> combos = new HashSet<>();

	private final BendingPlayer bendingPlayer;

	private final Scoreboard bendingBoard;
	private final Objective bendingSlots;
	private int selectedSlot;

	public BendingBoardInstance(final Player player, final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", "dummy", ChatColor.BOLD + "Slots");
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(bendingBoard);

		Arrays.fill(cachedSlots, "");
		updateAll();
	}

	public void disableScoreboard() {
		bendingBoard.clearSlot(DisplaySlot.SIDEBAR);
		bendingSlots.unregister();
		bendingPlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	private void setSlot(int slot, String name, boolean cooldown) {
		if (slot < 1 || slot > 9 || !bendingPlayer.getPlayer().getScoreboard().equals(bendingBoard)) return;
		StringBuilder sb = new StringBuilder(slot == selectedSlot ? ">" : "  ");
		if (name == null || name.isEmpty()) {
			sb.append(ChatColor.GRAY).append("-- Slot ").append(slot).append(" --");
		} else {
			CoreAbility coreAbility = CoreAbility.getAbility(ChatColor.stripColor(name));
			if (coreAbility == null) { // MultiAbility
				if (cooldown || bendingPlayer.isOnCooldown(name)) sb.append(ChatColor.STRIKETHROUGH);
				sb.append(name);
			} else {
				sb.append(coreAbility.getElement().getColor());
				if (cooldown || bendingPlayer.isOnCooldown(coreAbility)) sb.append(ChatColor.STRIKETHROUGH);
				sb.append(coreAbility.getName());
			}
		}
		sb.append(String.join("", Collections.nCopies(slot, ChatColor.RESET.toString())));

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

	public void updateCombo(String text, boolean show) {
		if (show) {
			if (combos.isEmpty()) {
				bendingSlots.getScore("  -----------  ").setScore(-10);
			}
			combos.add(text);
			bendingSlots.getScore(text).setScore(-10);

		} else {
			combos.remove(text);
			bendingBoard.resetScores(text);
			if (combos.isEmpty()) {
				bendingBoard.resetScores("  -----------  ");
			}
		}
	}
}
