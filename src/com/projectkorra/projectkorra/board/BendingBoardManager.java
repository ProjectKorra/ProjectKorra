package com.projectkorra.projectkorra.board;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages every individual {@link BendingBoard}
 */
public final class BendingBoardManager {
	
	private BendingBoardManager() {}
	
	private static final Set<String> disabledWorlds = new HashSet<>();
	private static final Map<String, ChatColor> trackedCooldowns = new ConcurrentHashMap<>();
	private static final Set<UUID> disabledPlayers = Collections.synchronizedSet(new HashSet<>());
	private static final Map<Player, BendingBoard> scoreboardPlayers = new ConcurrentHashMap<>();

	private static boolean enabled;

	public static void setup() {
		loadDisabledPlayers();
		initialize();
		Bukkit.getOnlinePlayers().forEach(BendingBoardManager::getBoard);
	}

	public static void reload() {
		scoreboardPlayers.values().forEach(BendingBoard::destroy);
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

	public static void changeWorld(Player player) {
		getBoard(player).ifPresent((b) -> b.setVisible(!disabledWorlds.contains(player.getWorld().getName())));
	}

	/**
	 * Toggles the bendingboard for the given player if the board is enabled and they are in a bending enabled world
	 * @param player Player with the bendingboard
	 * @param force True to ignore the enabled conditions and force a toggle update
	 */
	public static void toggleBoard(Player player, boolean force) {
		if (!force && (!enabled || disabledWorlds.contains(player.getWorld().getName()))) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Board.Disabled"));
			return;
		}

		if (scoreboardPlayers.containsKey(player)) {
			scoreboardPlayers.get(player).hide();
			disabledPlayers.add(player.getUniqueId());
			scoreboardPlayers.remove(player);
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Board.ToggledOff"));
		} else {
			disabledPlayers.remove(player.getUniqueId());
			getBoard(player).ifPresent(BendingBoard::show);
			ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Board.ToggledOn"));
		}
	}

	/**
	 * Gets the player's bendingboard if not disabled for some reason, and if it does not exist but
	 * should one will be created and stored for them
	 * @param player the player to get the bendingboard of
	 * @return empty if the board is disabled
	 */
	public static Optional<BendingBoard> getBoard(Player player) {
		if (!enabled || disabledPlayers.contains(player.getUniqueId()) || !player.hasPermission("bending.command.board")) {
			return Optional.empty();
		}

		if (!player.isOnline()) {
			return Optional.empty();
		}

		if (!scoreboardPlayers.containsKey(player)) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return Optional.empty();
			}
			
			scoreboardPlayers.put(player, new BendingBoard(bPlayer));
		}

		return Optional.of(scoreboardPlayers.get(player));
	}

	/**
	 * Sets the bendingboard to match the player's current slots, update the active slot, and match cooldowns
	 * @param player Player with the bendingboard, silently ignored if board is disabled
	 */
	public static void updateAllSlots(Player player) {
		getBoard(player).ifPresent(BendingBoard::updateAll);
	}

	/**
	 * Update the player's bendingboard based on the given information.
	 * <ul>
	 * <li>If the player has a multiability bound, all slots will be updated
	 * <li>If the name is null or empty, the given slot is cleared
	 * <li>Combos or names without an ability are updated on the extras portion
	 * <li>The specific slot is updated if is in bounds [1, 9]
	 * <li>The given ability name is set to match forceCooldown
	 * </ul>
	 * <br>
	 * @param player Player with the bendingboard, silently ignored if board is disabled
	 * @param name Name of the ability being affected
	 * @param forceCooldown Force if the name is strikethroughed on the board
	 * @param slot Slot being affected, use 0 if more than one slot is involved
	 */
	public static void updateBoard(Player player, String name, boolean forceCooldown, int slot) {
		getBoard(player).ifPresent((board) -> {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				scoreboardPlayers.get(player).updateAll();
			}
			
			if (name == null || name.isEmpty()) {
				scoreboardPlayers.get(player).clearSlot(slot);
				return;
			}
			
			CoreAbility coreAbility = CoreAbility.getAbility(name);
			if (coreAbility instanceof ComboAbility) {
				scoreboardPlayers.get(player).updateMisc(name, coreAbility.getElement().getColor(), forceCooldown);
			} else if (coreAbility == null && trackedCooldowns.containsKey(name)) {
				scoreboardPlayers.get(player).updateMisc(name, trackedCooldowns.get(name), forceCooldown);
			} else if (coreAbility != null && slot > 0) {
				scoreboardPlayers.get(player).setSlot(slot, name, forceCooldown);
			} else {
				scoreboardPlayers.get(player).setAbilityCooldown(name, forceCooldown);
			}
		});
	}

	/**
	 * Sets the active slot on the player's bendingboard
	 * @param player Player with the bendingboad, silently ignored if board is disabled
	 * @param newSlot New slot to be set as the active one
	 */
	public static void changeActiveSlot(Player player, int newSlot) {
		getBoard(player).ifPresent((board) -> board.setActiveSlot(newSlot));
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
