package com.projectkorra.projectkorra.object;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.storage.DBConnection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
	public static ConcurrentHashMap<UUID, List<Preset>> presets = new ConcurrentHashMap<UUID, List<Preset>>();
	static String loadQuery = "SELECT * FROM pk_presets WHERE uuid = ?";
	static String loadNameQuery = "SELECT * FROM pk_presets WHERE uuid = ? AND name = ?";
	static String deleteQuery = "DELETE FROM pk_presets WHERE uuid = ? AND name = ?";
	static String insertQuery = "INSERT INTO pk_presets (uuid, name) VALUES (?, ?)";
	static String updateQuery1 = "UPDATE pk_presets SET slot";
	static String updateQuery2 = " = ? WHERE uuid = ? AND name = ?";

	UUID uuid;
	HashMap<Integer, String> abilities;
	String name;

	/**
	 * Creates a new {@link Preset}
	 * 
	 * @param uuid The UUID of the Player who the Preset belongs to
	 * @param name The name of the Preset
	 * @param abilities A HashMap of the abilities to be saved in the Preset,
	 *            keyed to the slot they're bound to
	 */
	public Preset(UUID uuid, String name, HashMap<Integer, String> abilities) {
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
	public static void unloadPreset(Player player) {
		UUID uuid = player.getUniqueId();
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
				UUID uuid = player.getUniqueId();
				if (uuid == null)
					return;
				try {
					PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(loadQuery);
					ps.setString(1, uuid.toString());
					ResultSet rs = ps.executeQuery();
					if (rs.next()) { // Presets exist.
						int i = 0;
						do {
							HashMap<Integer, String> moves = new HashMap<Integer, String>();
							for (int total = 1; total <= 9; total++) {
								String slot = rs.getString("slot" + total);
								if (slot != null)
									moves.put(total, slot);
							}
							new Preset(uuid, rs.getString("name"), moves);
							i++;
						}
						while (rs.next());
						ProjectKorra.log.info("Loaded " + i + " presets for " + player.getName());
					}
				}
				catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}.runTaskAsynchronously(ProjectKorra.plugin);
	}

	/**
	 * Binds the abilities from a Preset for the given Player.
	 * 
	 * @param player The Player the Preset should be bound for
	 * @param name The name of the Preset that should be bound
	 * @return True if all abilities were successfully bound, or false otherwise
	 */
	@SuppressWarnings("unchecked")
	public static boolean bindPreset(Player player, String name) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer == null)
			return false;
		if (!presets.containsKey(player.getUniqueId()))
			return false;
		HashMap<Integer, String> abilities = null;
		for (Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name)) { // We found it
				abilities = (HashMap<Integer, String>) preset.abilities.clone();
			}
		}
		if (abilities == null) {

		}
		boolean boundAll = true;
		for (int i = 1; i <= 9; i++) {
			if (!GeneralMethods.canBend(player.getName(), abilities.get(i))) {
				abilities.remove(i);
				boundAll = false;
			}
		}
		bPlayer.setAbilities(abilities);
		return boundAll;
	}

	/**
	 * Checks if a Preset with a certain name exists for a given Player.
	 * 
	 * @param player The player who's Presets should be checked
	 * @param name The name of the Preset to look for
	 * @return true if the Preset exists, false otherwise
	 */
	public static boolean presetExists(Player player, String name) {
		if (!presets.containsKey(player.getUniqueId()))
			return false;
		boolean exists = false;
		for (Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name))
				exists = true;
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
	public static Preset getPreset(Player player, String name) {
		if (!presets.containsKey(player.getUniqueId()))
			return null;
		for (Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name))
				return preset;
		}
		return null;
	}

	/**
	 * Gets the contents of a Preset for the specified Player.
	 * 
	 * @param player The Player who's Preset should be gotten
	 * @param name The name of the Preset who's contents should be gotten
	 * @return HashMap of ability names keyed to hotbar slots, if the Preset
	 *         exists, or null otherwise
	 */
	public static HashMap<Integer, String> getPresetContents(Player player, String name) {
		if (!presets.containsKey(player.getUniqueId()))
			return null;
		for (Preset preset : presets.get(player.getUniqueId())) {
			if (preset.name.equalsIgnoreCase(name)) {
				return preset.abilities;
			}
		}
		return null;
	}

	/**
	 * Deletes the Preset from the database.
	 */
	public void delete() {
		try {
			PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(deleteQuery);
			ps.setString(1, uuid.toString());
			ps.setString(2, name);
			ps.execute();
			presets.get(uuid).remove(this);
		}
		catch (SQLException e) {
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
	public void save() {
		try {
			PreparedStatement ps = DBConnection.sql.getConnection().prepareStatement(loadNameQuery);
			ps.setString(1, uuid.toString());
			ps.setString(2, name);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) { //if the preset doesn't exist in the DB, create it
				ps = DBConnection.sql.getConnection().prepareStatement(insertQuery);
				ps.setString(1, uuid.toString());
				ps.setString(2, name);
				ps.execute();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		for (Integer i : abilities.keySet()) {
			new BukkitRunnable() {
				PreparedStatement ps;

				@Override
				public void run() {
					try {
						ps = DBConnection.sql.getConnection().prepareStatement(updateQuery1 + i + updateQuery2);
						ps.setString(1, abilities.get(i));
						ps.setString(2, uuid.toString());
						ps.setString(3, name);
						ps.execute();
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		}
	}
}
