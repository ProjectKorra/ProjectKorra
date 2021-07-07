package com.projectkorra.projectkorra.board;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import net.md_5.bungee.api.ChatColor;

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
		
		@SuppressWarnings("deprecation")
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
	
	private final BoardSlot[] slots = new BoardSlot[9];
	private final Map<String, BoardSlot> misc = new HashMap<>();
	private BoardSlot miscTail = null;

	private final Player player;
	private final BendingPlayer bendingPlayer;

	private final Scoreboard bendingBoard;
	private final Objective bendingSlots;
	private int selectedSlot;
	
	private String prefix, emptySlot, miscSeparator;
	private ChatColor selectedColor, altColor;

	public BendingBoardInstance(final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		player = bPlayer.getPlayer();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		String title = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.Title"));
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", "dummy", title);
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		player.setScoreboard(bendingBoard);
		
		for (int i = 0; i < 9; ++i) {
			slots[i] = new BoardSlot(bendingBoard, bendingSlots, i);
		}
		
		prefix = ChatColor.stripColor(ConfigManager.languageConfig.get().getString("Board.Prefix.Text"));
		emptySlot = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.EmptySlot"));
		miscSeparator = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.MiscSeparator"));

		updateColors();
		updateAll();
	}
	
	private ChatColor getElementColor() {
		if (bendingPlayer.getElements().size() > 1) {
			return Element.AVATAR.getColor().asBungee();
		} else if (bendingPlayer.getElements().size() == 1) {
			return bendingPlayer.getElements().get(0).getColor().asBungee();
		} else {
			return ChatColor.WHITE;
		}
	}
	
	private ChatColor getColor(String from, ChatColor def) {
		if (from.equalsIgnoreCase("element")) {
			return getElementColor();
		}
		
		try {
			return ChatColor.of(from);
		} catch (Exception e) {
			ProjectKorra.plugin.getLogger().warning("Couldn't parse board color from '" + from + "', using default!");
			return def;
		}
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
		
		slots[slot - 1].update((slot == selectedSlot ? selectedColor : altColor) + prefix, sb.toString());
	}
	
	private int updateSelected(int newSlot) {
		int oldSlot = selectedSlot;
		selectedSlot = newSlot;
		return oldSlot;
	}
	
	public void updateColors() {
		selectedColor = getColor(ConfigManager.languageConfig.get().getString("Board.Prefix.SelectedColor"), ChatColor.WHITE);
		altColor = getColor(ConfigManager.languageConfig.get().getString("Board.Prefix.NonSelectedColor"), ChatColor.DARK_GRAY);
	}

	public void updateAll() {
		updateColors();
		updateSelected(player.getInventory().getHeldItemSlot() + 1);
		for (int i = 1; i <= 9; i++) {
			setSlot(i, bendingPlayer.getAbilities().get(i), false);
		}
	}

	public void clearSlot(int slot) {
		setSlot(slot, null, false);
	}

	public void setActiveSlot(int newSlot) {
		int oldSlot = updateSelected(newSlot);
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
