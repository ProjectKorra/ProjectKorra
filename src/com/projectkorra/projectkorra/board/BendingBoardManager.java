package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.storage.DBConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages every individual {@link BendingBoardInstance}
 */
public class BendingBoardManager {
	private static final Set<String> disabledWorlds = new HashSet<>();
	private static final Map<String, ChatColor> trackedCooldowns = new ConcurrentHashMap<>();
	private static final Set<UUID> disabledPlayers = Collections.synchronizedSet(new HashSet<>());
	private static final Map<Player, BendingBoardInstance> scoreboardPlayers = new ConcurrentHashMap<>();

	private static boolean enabled;

	public static void setup() {
		initialize();
		for (Player player : Bukkit.getOnlinePlayers()) {
			canUseScoreboard(player);
		}
	}

	public static void reload() {
		scoreboardPlayers.values().forEach(BendingBoardInstance::disableScoreboard);
		disabledWorlds.clear();
		trackedCooldowns.clear();
		scoreboardPlayers.clear();
		initialize();
	}

	private static void initialize() {
		loadDisabledPlayers();
		enabled = ConfigManager.defaultConfig.get().getBoolean("Properties.BendingBoard");
		disabledWorlds.addAll(ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds"));
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
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) return false;
			scoreboardPlayers.put(player, new BendingBoardInstance(bPlayer));
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
				scoreboardPlayers.get(player).updateMisc("  " + coreAbility.getElement().getColor() + ChatColor.STRIKETHROUGH + abilityName, cooldown, true);
				return;
			} else if (coreAbility == null && trackedCooldowns.containsKey(abilityName)) {
				scoreboardPlayers.get(player).updateMisc("  " + trackedCooldowns.get(abilityName) + ChatColor.STRIKETHROUGH + abilityName, cooldown, false);
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

	/**
	 * Some abilities use internal cooldowns with custom names that don't correspond to bound abilities' names.
	 * Adds the internal cooldown name and color to the map of tracked abilities so as they can appear on the bending baord.
	 * @param cooldownName the internal cooldown name
	 * @param color the color to use when rendering the board entry
	 */
	public static void addCooldownToTrack(String cooldownName, ChatColor color) {
		if (CoreAbility.getAbility(cooldownName) != null) return; // Ignore cooldown if already corresponds to a CoreAbility name
		trackedCooldowns.put(cooldownName, color);
	}

	/**
	 * Load into memory the list of players who have toggled the bending board off.
	 */
	public static void loadDisabledPlayers() {
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					disabledPlayers.clear();
					final ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_board where enabled = 0");
					while (rs.next()) {
						disabledPlayers.add(UUID.fromString(rs.getString("uuid")));
					}
				} catch (final SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ProjectKorra.plugin);
	}

	/**
	 * Called on player logout
	 * Removes the board instance and stores the player's toggle preference for the bending board in the database
	 * @param player
	 */
	public static void clean(final Player player) {
		scoreboardPlayers.remove(player);
		final UUID uuid = player.getUniqueId();
		StringBuilder updateQuery = new StringBuilder("UPDATE pk_board SET enabled = ");
		updateQuery.append(disabledPlayers.contains(uuid) ? 0 : 1);
		updateQuery.append(" WHERE uuid = ?");
		new BukkitRunnable() {
			@Override
				public void run() {
				try {
					PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement("SELECT * FROM pk_board where uuid = ? LIMIT 1");
					ps.setString(1, uuid.toString());
					if (!ps.executeQuery().next()) { // if the entry doesn't exist in the DB, create it.
						ps = DBConnection.sql.getConnection().prepareStatement("INSERT INTO pk_board (uuid, enabled) VALUES (?, 1)");
					} else { // if the entry exists in the DB, update it
						ps = DBConnection.sql.getConnection().prepareStatement(updateQuery.toString());
					}
					ps.setString(1, uuid.toString());
					ps.execute();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ProjectKorra.plugin);
	}
}
