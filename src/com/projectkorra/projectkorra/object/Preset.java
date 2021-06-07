package com.projectkorra.projectkorra.object;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.storage.DBConnection;

/**
 * A savable association of abilities and hotbar slots, stored per player.
 *
 * @author kingbirdy
 *
 */
public class Preset {

	/**
	 * ConcurrentHashMap that stores a list of every Player's {@link Preset
	 * presets}, keyed to their UUID
	 */
	public static Map<UUID, List<Preset>> presets = new ConcurrentHashMap<UUID, List<Preset>>();
	public static FileConfiguration config = ConfigManager.presetConfig.get();
	public static HashMap<String, ArrayList<String>> externalPresets = new HashMap<String, ArrayList<String>>();
	static String loadQuery = "SELECT * FROM pk_presets WHERE uuid = ?";
	static String loadNameQuery = "SELECT * FROM pk_presets WHERE uuid = ? AND name = ?";
	static String deleteQuery = "DELETE FROM pk_presets WHERE uuid = ? AND name = ?";
	static String insertQuery = "INSERT INTO pk_presets (uuid, name) VALUES (?, ?)";
	static String updateQuery1 = "UPDATE pk_presets SET slot";
	static String updateQuery2 = " = ? WHERE uuid = ? AND name = ?";

	private final UUID uuid;
	private final HashMap<Integer, String> abilities;
	private final String name;

	/**
	 * Creates a new {@link Preset}
	 *
	 * @param uuid The UUID of the Player who the Preset belongs to
	 * @param name The name of the Preset
	 * @param abilities A HashMap of the abilities to be saved in the Preset,
	 *            keyed to the slot they're bound to
	 */
	public Preset(final UUID uuid, final String name, final HashMap<Integer, String> abilities) {
		this.uuid = uuid;
		this.name = name;
		this.abilities = abilities;
		if (!presets.containsKey(uuid)) {
			presets.put(uuid, new ArrayList<Preset>());
		}
		presets.get(uuid).add(this);
	}

	/**
	 * Unload a Player's Presets from those stored in memory.
	 *
	 * @param player The Player who's Presets should be unloaded
	 */
	public static void unloadPreset(final Player player) {
		final UUID uuid = player.getUniqueId();
		presets.remove(uuid);
	}

	/**
	 * Load a Player's Presets into memory.
	 *
	 * @param player The Player who's Presets should be loaded
	 */
	public static void loadPresets(final Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				final UUID uuid = player.getUniqueId();
				if (uuid == null) {
					return;
				}
				try {
					final PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(loadQuery);
					ps.setString(1, uuid.toString());
					final ResultSet rs = ps.executeQuery();
					if (rs.next()) { // Presets exist.
						int i = 0;
						do {
							final HashMap<Integer, String> moves = new HashMap<Integer, String>();
							for (int total = 1; total <= 9; total++) {
								final String slot = rs.getString("slot" + total);
								if (slot != null) {
									moves.put(total, slot);
								}
							}
							new Preset(uuid, rs.getString("name"), moves);
							i++;
						} while (rs.next());
						ProjectKorra.log.info("Loaded " + i + " presets for " + player.getName());
					}
				} catch (final SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ProjectKorra.plugin);
	}

	/**
	 * Reload a Player's Presets from those stored in memory.
	 *
	 * @param player The Player who's Presets should be unloaded
	 */
	public static void reloadPreset(final Player player) {
		unloadPreset(player);
		loadPresets(player);
	}

	/**
	 * Binds the abilities from a Preset for the given Player.
	 *
	 * @param player The Player the Preset should be bound for
	 * @param name The name of the Preset that should be bound
	 * @return True if all abilities were successfully bound, or false otherwise
	 */
	public static boolean bindPreset(final Player player, final Preset preset) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		final HashMap<Integer, String> abilities = new HashMap<>(preset.abilities);
		boolean boundAll = true;
		for (int i = 1; i <= 9; i++) {
			final CoreAbility coreAbil = CoreAbility.getAbility(abilities.get(i));
			if (coreAbil != null && !bPlayer.canBind(coreAbil)) {
				abilities.remove(i);
				boundAll = false;
			}
		}
		bPlayer.setAbilities(abilities);
		BendingBoardManager.updateAllSlots(player);
		return boundAll;
	}

	/**
	 * Checks if a Preset with a certain name exists for a given Player.
	 *
	 * @param player The player who's Presets should be checked
	 * @param name The name of the Preset to look for
	 * @return true if the Preset exists, false otherwise
	 */
	public static boolean presetExists(final Player player, final String name) {
		if (!presets.containsKey(player.getUniqueId())) {
			return false;
		}
		boolean exists = false;
		for (final Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name)) {
				exists = true;
			}
		}
		return exists;
	}

	/**
	 * Gets a Preset for the specified Player.
	 *
	 * @param Player The Player who's Preset should be gotten
	 * @param name The name of the Preset to get
	 * @return The Preset, if it exists, or null otherwise
	 */
	public static Preset getPreset(final Player player, final String name) {
		if (!presets.containsKey(player.getUniqueId())) {
			return null;
		}
		for (final Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name)) {
				return preset;
			}
		}
		return null;
	}

	public static void loadExternalPresets() {
		final HashMap<String, ArrayList<String>> presets = new HashMap<String, ArrayList<String>>();
		for (final String name : config.getKeys(false)) {
			if (!presets.containsKey(name)) {
				if (!config.getStringList(name).isEmpty() && config.getStringList(name).size() <= 9) {
					presets.put(name.toLowerCase(), (ArrayList<String>) config.getStringList(name));
				}
			}
		}
		externalPresets = presets;
	}

	public static boolean externalPresetExists(final String name) {
		for (final String preset : externalPresets.keySet()) {
			if (name.equalsIgnoreCase(preset)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the contents of a Preset for the specified Player.
	 *
	 * @param player The Player who's Preset should be gotten
	 * @param name The name of the Preset who's contents should be gotten
	 * @return HashMap of ability names keyed to hotbar slots, if the Preset
	 *         exists, or null otherwise
	 */
	public static HashMap<Integer, String> getPresetContents(final Player player, final String name) {
		if (!presets.containsKey(player.getUniqueId())) {
			return null;
		}
		for (final Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name)) {
				return preset.abilities;
			}
		}
		return null;
	}

	public static boolean bindExternalPreset(final Player player, final String name) {
		boolean boundAll = true;
		int slot = 0;
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		final HashMap<Integer, String> abilities = new HashMap<Integer, String>();

		if (externalPresetExists(name.toLowerCase())) {
			for (final String ability : externalPresets.get(name.toLowerCase())) {
				slot++;
				final CoreAbility coreAbil = CoreAbility.getAbility(ability);
				if (coreAbil != null) {
					abilities.put(slot, coreAbil.getName());
				}
			}

			for (int i = 1; i <= 9; i++) {
				final CoreAbility coreAbil = CoreAbility.getAbility(abilities.get(i));
				if (coreAbil != null && !bPlayer.canBind(coreAbil)) {
					abilities.remove(i);
					boundAll = false;
				}
			}
			bPlayer.setAbilities(abilities);
			BendingBoardManager.updateAllSlots(player);
			return boundAll;
		}
		return false;
	}

	/**
	 * Deletes the Preset from the database.
	 */
	public void delete() {
		try {
			final PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(deleteQuery);
			ps.setString(1, this.uuid.toString());
			ps.setString(2, this.name);
			ps.execute();
			presets.get(this.uuid).remove(this);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the name of the preset.
	 *
	 * @return The name of the preset
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Saves the Preset to the database.
	 */
	public void save(final Player player) {
		try {
			PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(loadNameQuery);
			ps.setString(1, this.uuid.toString());
			ps.setString(2, this.name);
			final ResultSet rs = ps.executeQuery();
			if (!rs.next()) { // if the preset doesn't exist in the DB, create it.
				ps = DBConnection.sql.getConnection().prepareStatement(insertQuery);
				ps.setString(1, this.uuid.toString());
				ps.setString(2, this.name);
				ps.execute();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		for (final Integer i : this.abilities.keySet()) {
			new BukkitRunnable() {
				PreparedStatement ps;

				@Override
				public void run() {
					try {
						this.ps = DBConnection.sql.getConnection().prepareStatement(updateQuery1 + i + updateQuery2);
						this.ps.setString(1, Preset.this.abilities.get(i));
						this.ps.setString(2, Preset.this.uuid.toString());
						this.ps.setString(3, Preset.this.name);
						this.ps.execute();
					} catch (final SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1500);
					reloadPreset(player);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ProjectKorra.plugin);
	}
}
