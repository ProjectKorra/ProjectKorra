package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a player's scoreboard for bending purposes
 */
public class BendingBoard {
	
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
			this.slot = slot + 1;
			this.formTeam();
		}

		@SuppressWarnings("deprecation")
		private void formTeam() {
			this.team = board.registerNewTeam("slot" + this.slot);
			this.entry = ChatColor.values()[slot % 10] + "" + ChatColor.values()[slot % 16];
			
			team.addEntry(entry);
		}
		
		private void set() {
			obj.getScore(entry).setScore(-slot);
		}
		
		public void update(String prefix, String name) {
			team.setPrefix(prefix);
			team.setSuffix(name);
			set();
		}
		
		public void setSlot(int slot) {
			this.slot = slot + 1;
			set();
		}

		public void decreaseSlot() {
			--this.slot;
			clear(true);
		}

		public void clear(boolean formNewTeam) {
			String prefix = team.getPrefix(), suffix = team.getSuffix();
			board.resetScores(entry);
			team.unregister();
			if (formNewTeam) {
				formTeam();
				update(prefix, suffix);
			}
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

	public BendingBoard(final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		player = bPlayer.getPlayer();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		String title = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.Title"));
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", "dummy", title);
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (int i = 0; i < 9; ++i) {
			slots[i] = new BoardSlot(bendingBoard, bendingSlots, i);
		}
		
		prefix = ChatColor.stripColor(ConfigManager.languageConfig.get().getString("Board.Prefix.Text"));
		emptySlot = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.EmptySlot"));
		miscSeparator = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Board.MiscSeparator"));

		updateAll();
	}

	void destroy() {
		bendingBoard.clearSlot(DisplaySlot.SIDEBAR);
		bendingSlots.unregister();
	}
	
	private ChatColor getElementColor() {
		if (bendingPlayer.getElements().size() > 1) {
			return Element.AVATAR.getColor();
		} else if (bendingPlayer.getElements().size() == 1) {
			return bendingPlayer.getElements().get(0).getColor();
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

	public void hide() {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	public void show() {
		player.setScoreboard(bendingBoard);
		updateAll();
	}

	public boolean isVisible() {
		return player.getScoreboard().equals(bendingBoard);
	}

	public void setVisible(boolean show) {
		if (show) {
			show();
		} else {
			hide();
		}
	}

	public void setSlot(int slot, String ability, boolean cooldown) {
		if (slot < 1 || slot > 9 || !player.getScoreboard().equals(bendingBoard)) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (ability == null || ability.isEmpty()) {
			sb.append(emptySlot.replaceAll("\\{slot_number\\}", "" + slot));
		} else {
			CoreAbility coreAbility = CoreAbility.getAbility(ChatColor.stripColor(ability));
			if (coreAbility == null) { // MultiAbility
				if (cooldown || bendingPlayer.isOnCooldown(ability)) {
					sb.append(ChatColor.STRIKETHROUGH);
				}
				
				sb.append(ability);
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
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;
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

	public void setAbilityCooldown(String name, boolean cooldown) {
		bendingPlayer.getAbilities().entrySet().stream().filter(entry -> name.equals(entry.getValue())).forEach(entry -> setSlot(entry.getKey(), name, cooldown));
	}
	
	public void updateMisc(String name, ChatColor color, boolean cooldown) {
		if (!cooldown) {
			misc.computeIfPresent(name, (key, slot) -> {
				if (slot == miscTail) {
					miscTail = null;
				}
				
				slot.clear(false);
				return null;
			});
				
			if (misc.isEmpty()) {
				bendingBoard.resetScores(miscSeparator);
			}
		} else if (!misc.containsKey(name)) {
			BoardSlot slot = new BoardSlot(bendingBoard, bendingSlots, 10 + misc.size());
			slot.update(String.join("", Collections.nCopies(ChatColor.stripColor(prefix).length() + 1, " ")), color + "" + ChatColor.STRIKETHROUGH + name);
			
			if (miscTail != null) {
				miscTail.setNext(slot);
			}
			
			miscTail = slot;
			misc.put(name, slot);
			bendingSlots.getScore(miscSeparator).setScore(-10);
		}	
	}
}
