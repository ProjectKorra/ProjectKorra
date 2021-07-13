package com.projectkorra.projectkorra.board;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.storage.DBConnection;

import net.md_5.bungee.api.ChatColor;

/**
 * Manages every individual {@link BendingBoardInstance}
 */
public final class BendingBoardManager {
	
	private BendingBoardManager() {}
	
	private static final Set<String> disabledWorlds = new HashSet<>();
	private static final Map<String, ChatColor> trackedCooldowns = new ConcurrentHashMap<>();
	private static final Set<UUID> disabledPlayers = Collections.synchronizedSet(new HashSet<>());
	private static final Map<Player, BendingBoardInstance> scoreboardPlayers = new ConcurrentHashMap<>();

	private static boolean enabled;

	public static void setup() {
		loadDisabledPlayers();
		initialize();
		Bukkit.getOnlinePlayers().forEach(BendingBoardManager::canUseScoreboard);
	}

	public static void reload() {
		scoreboardPlayers.values().forEach(BendingBoardInstance::disableScoreboard);
		scoreboardPlayers.clear();
		initialize();
	}

	private static void initialize() {
		enabled = ConfigManager.getConfig().getBoolean("Properties.BendingBoard");
		
		disabledWorlds.clear();
		disabledWorlds.addAll(ConfigManager.getConfig().getStringList("Properties.DisabledWorlds"));
		
		if (ConfigManager.languageConfig.get().contains("Board.Extras")) {
			ConfigurationSection section = ConfigManager.languageConfig.get().getConfigurationSection("Board.Extras");
			for (String key : section.getKeys(false)) {
				try {
					trackedCooldowns.put(key, ChatColor.of(section.getString(key)));
				} catch (Exception e) {
					ProjectKorra.plugin.getLogger().warning("Couldn't parse color from 'Board.Extras." + key + "', using white.");
					trackedCooldowns.put(key, ChatColor.WHITE);
				}
			}
		}
	}
	
	/**
	 * Check if the player has the BendingBoard toggled off (disabled)
	 * @param player {@link Player} to check toggled
	 * @return true if player has the BendingBoard toggled off
	 */
	public static boolean isDisabled(Player player) {
		return disabledPlayers.contains(player.getUniqueId());
	}

	/**
	 * Force toggle the scoreboard for when a player changes worlds (for example when teleporting to a world where bending is disabled)
	 * @param player {@link Player} to force toggle for
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

		if (!player.isOnline()) {
			return false;
		}

		if (!scoreboardPlayers.containsKey(player)) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return false;
			}
			
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
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				scoreboardPlayers.get(player).updateAll();
			}
			
			if (abilityName == null || abilityName.isEmpty()) {
				scoreboardPlayers.get(player).clearSlot(slot);
				return;
			}
			
			CoreAbility coreAbility = CoreAbility.getAbility(abilityName);
			if (coreAbility != null && coreAbility instanceof ComboAbility) {
				scoreboardPlayers.get(player).updateMisc(abilityName, coreAbility.getElement().getColor().asBungee(), cooldown);
			} else if (coreAbility == null && trackedCooldowns.containsKey(abilityName)) {
				scoreboardPlayers.get(player).updateMisc(abilityName, trackedCooldowns.get(abilityName), cooldown);
			} else if (coreAbility != null && slot > 0) {
				scoreboardPlayers.get(player).setSlot(slot, abilityName, cooldown);
			} else {
				scoreboardPlayers.get(player).setAbilityCooldown(abilityName, cooldown);
			}
		}
	}

	public static void changeActiveSlot(Player player, int newSlot) {
		if (canUseScoreboard(player)) {
			scoreboardPlayers.get(player).setActiveSlot(++newSlot);
		}
	}

	/**
	 * Some abilities use internal cooldowns with custom names that don't correspond to bound abilities' names.
	 * Adds the internal cooldown name and color to the map of tracked abilities so as they can appear on the bending board.
	 * @param cooldownName the internal cooldown name
	 * @param color the color to use when rendering the board entry
	 */
	public static void addCooldownToTrack(String cooldownName, ChatColor color) {
		trackedCooldowns.put(cooldownName, color);
	}

	/**
	 * Load into memory the list of players who have toggled the bending board off.
	 */
	public static void loadDisabledPlayers() {
		Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
			Set<UUID> disabled = new HashSet<>();
			try {
				final ResultSet rs = DBConnection.sql.readQuery("SELECT uuid FROM pk_board WHERE enabled = 0");
				while (rs.next()) disabled.add(UUID.fromString(rs.getString("uuid")));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			disabledPlayers.clear();
			disabledPlayers.addAll(disabled);
		});
	}

	/**
	 * Called on player logout
	 * Removes the board instance and stores the player's toggle preference for the bending board in the database
	 * @param player
	 */
	public static void clean(final Player player) {
		scoreboardPlayers.remove(player);
		final UUID uuid = player.getUniqueId();
		final String updateQuery = "UPDATE pk_board SET enabled = " + (disabledPlayers.contains(uuid) ? 0 : 1) + " WHERE uuid = ?";
		Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
			try {
				PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement("SELECT enabled FROM pk_board WHERE uuid = ? LIMIT 1");
				ps.setString(1, uuid.toString());
				PreparedStatement ps2;
				if (!ps.executeQuery().next()) { // if the entry doesn't exist in the DB, create it.
					ps2 = DBConnection.sql.getConnection().prepareStatement("INSERT INTO pk_board (uuid, enabled) VALUES (?, 1)");
				} else { // if the entry exists in the DB, update it
					ps2 = DBConnection.sql.getConnection().prepareStatement(updateQuery);
				}
				ps2.setString(1, uuid.toString());
				ps2.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
}
