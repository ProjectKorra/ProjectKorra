package com.projectkorra.projectkorra.board;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Represents a player's scoreboard for bending purposes
 */
public class BendingBoardInstance {
	
	public static class BoardSlot {
		
		private Scoreboard board;
		private Objective obj;
		private int slot;
		private Team team;
		private String entry;
		private Optional<BoardSlot> next = Optional.empty();
		
		public BoardSlot(Scoreboard board, Objective obj, int slot) {
			this.board = board;
			this.obj = obj;
			this.slot = slot;
			this.team = board.registerNewTeam("slot" + slot);
			this.entry = ChatColor.values()[slot % 10] + "" + ChatColor.values()[slot % 16];
			
			team.addEntry(entry);
		}
		
		public void update(String prefix, String name) {
			team.setPrefix(prefix);
			team.setSuffix(name);
			obj.getScore(entry).setScore(-slot);
		}
		
		public void setSlot(int slot) {
			this.slot = slot;
			obj.getScore(entry).setScore(-slot);
		}
		
		public void decreaseSlot() {
			setSlot(--slot);
			next.ifPresent(BoardSlot::decreaseSlot);
		}
		
		public void clear() {
			board.resetScores(entry);
			team.unregister();
			next.ifPresent(BoardSlot::decreaseSlot);
		}
		
		private void setNext(BoardSlot slot) {
			this.next = Optional.ofNullable(slot);
		}
	}
	
	private final BoardSlot[] slots = new BoardSlot[10];
	private final Map<String, BoardSlot> misc = new HashMap<>();
	private BoardSlot miscTail = null;

	private final Player player;
	private final BendingPlayer bendingPlayer;

	private final Scoreboard bendingBoard;
	private final Objective bendingSlots;
	private int selectedSlot;
	
	private String prefix, altPrefix, emptySlot, miscSeparator;

	public BendingBoardInstance(final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		player = bPlayer.getPlayer();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		String title = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.Title"));
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", "dummy", title);
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(bendingBoard);
		
		for (int i = 0; i < 10; ++i) {
			slots[i] = new BoardSlot(bendingBoard, bendingSlots, i);
		}
		
		prefix = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.SelectionPrefix"));
		altPrefix = ChatColor.BLACK + ChatColor.stripColor(prefix);
		emptySlot = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.EmptySlot"));
		miscSeparator = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.MiscSeparator"));

		updateAll();
	}

	public void disableScoreboard() {
		bendingBoard.clearSlot(DisplaySlot.SIDEBAR);
		bendingSlots.unregister();
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	private void setSlot(int slot, String name, boolean cooldown) {
		if (slot < 1 || slot > 9 || !player.getScoreboard().equals(bendingBoard)) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (name == null || name.isEmpty()) {
			sb.append(emptySlot.replaceAll("\\{slot_number\\}", "" + slot));
		} else {
			CoreAbility coreAbility = CoreAbility.getAbility(ChatColor.stripColor(name));
			if (coreAbility == null) { // MultiAbility
				if (cooldown || bendingPlayer.isOnCooldown(name)) {
					sb.append(ChatColor.STRIKETHROUGH);
				}
				
				sb.append(name);
			} else {
				sb.append(coreAbility.getMovePreviewWithoutCooldownTimer(player, cooldown));
			}
		}
		
		slots[slot].update(slot == selectedSlot ? prefix : altPrefix, sb.toString());
	}

	public void updateAll() {
		for (int i = 1; i <= 9; i++) {
			setSlot(i, bendingPlayer.getAbilities().get(i), false);
		}
	}

	public void clearSlot(int slot) {
		setSlot(slot, null, false);
	}

	public void setActiveSlot(int newSlot) {
		int oldSlot = selectedSlot;
		selectedSlot = newSlot;
		setSlot(oldSlot, bendingPlayer.getAbilities().get(oldSlot), false);
		setSlot(newSlot, bendingPlayer.getAbilities().get(newSlot), false);
	}

	public void setAbility(String name, boolean cooldown) {
		bendingPlayer.getAbilities().entrySet().stream().filter(entry -> name.equals(entry.getValue())).forEach(entry -> setSlot(entry.getKey(), name, cooldown));
	}
	
	public void updateMisc(String name, ChatColor color) {
		if (misc.containsKey(name)) {
			misc.get(name).clear();
			
			if (misc.get(name) == miscTail) {
				miscTail = null;
			}
			
			misc.remove(name);
		} else {
			BoardSlot slot = new BoardSlot(bendingBoard, bendingSlots, 11 + misc.size());
			slot.update(String.join("", Collections.nCopies(ChatColor.stripColor(prefix).length() + 1, " ")), color + "" + ChatColor.STRIKETHROUGH + name);
			
			if (miscTail != null) {
				miscTail.setNext(slot);
			}
			
			miscTail = slot;
			misc.put(name, slot);
			bendingSlots.getScore(miscSeparator).setScore(-10);
		}
		
		if (misc.isEmpty()) {
			bendingBoard.resetScores(miscSeparator);
		}
	}
}
