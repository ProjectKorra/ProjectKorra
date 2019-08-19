package com.projectkorra.projectkorra.object;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.storage.DBConnection;

/**
 * A persistent association of abilities and hotbar slots, stored per player.
 */
@SuppressWarnings("rawtypes")
public class Preset {

	/**
	 * ConcurrentHashMap that stores a list of every Player's {@link Preset
	 * presets}, keyed to their UUID
	 */
	public static Map<UUID, List<Preset>> presets = new ConcurrentHashMap<>();
	public static Map<String, ExternalPreset> externalPresets = new HashMap<>();
	static String loadQuery = "SELECT * FROM pk_presets WHERE uuid = ?";
	static String loadNameQuery = "SELECT * FROM pk_presets WHERE uuid = ? AND name = ?";
	static String deleteQuery = "DELETE FROM pk_presets WHERE uuid = ? AND name = ?";
	static String insertQuery = "INSERT INTO pk_presets (uuid, name) VALUES (?, ?)";
	static String updateQuery1 = "UPDATE pk_presets SET slot";
	static String updateQuery2 = " = ? WHERE uuid = ? AND name = ?";

	private final transient UUID uuid;
	private final transient String[] abilities;
	private final transient String name;

	/**
	 * Creates a new {@link Preset}
	 *
	 * @param uuid The UUID of the Player who the Preset belongs to
	 * @param name The name of the Preset
	 * @param abilities A HashMap of the abilities to be saved in the Preset,
	 *            keyed to the slot they're bound to
	 */
	public Preset(final UUID uuid, final String name, final String[] abilities) {
		this.uuid = uuid;
		this.name = name;
		this.abilities = abilities;
		if (!presets.containsKey(uuid)) {
			presets.put(uuid, new ArrayList<Preset>());
		}
		presets.get(uuid).add(this);
	}
	
	public static final class ExternalPreset extends Preset {
		
		private final String Name = null;
		private final String[] Abilities = null;
		
		public ExternalPreset() {
			super(null, null, null);
		}
		
		@Override
		public String getName() {
			return Name;
		}
		
		@Override
		public String[] getAbilities() {
			return Abilities;
		}
		
		@Override
		public void delete() {}
		
		@Override
		public void save(final Player player) {}
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
							String[] abilities = new String[9];
							for (int slot = 0; slot < 9; slot++) {
								abilities[slot] = rs.getString("slot" + (slot + 1));
							}
							new Preset(uuid, rs.getString("name"), abilities);
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

		String[] abilities = preset.getAbilities();
		boolean boundAll = true;
		for (int i = 0; i < abilities.length; i++) {
			final CoreAbility coreAbil = CoreAbility.getAbility(abilities[i]);
			if (coreAbil != null && !bPlayer.canBind(coreAbil)) {
				abilities[i] = null;
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
		Gson gson = new Gson();
		File directory = new File(JavaPlugin.getPlugin(ProjectKorra.class).getDataFolder(), "presets");
		if (directory.exists() && directory.isDirectory()) {
			for (File f : directory.listFiles((parent, name) -> name.endsWith(".json"))) {
				try (BufferedReader reader = Files.newBufferedReader(f.toPath())) {
					ExternalPreset preset = gson.fromJson(reader, ExternalPreset.class);
					externalPresets.put(preset.getName().toLowerCase(), preset);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			directory.mkdirs();
		}
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
	 * @return String array of ability names indexed by hotbar slots, if the Preset
	 *         exists, or null otherwise
	 */
	public static String[] getPresetContents(final Player player, final String name) {
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
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		if (externalPresetExists(name.toLowerCase())) {
			return bindPreset(player, externalPresets.get(name.toLowerCase()));
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
	
	public String[] getAbilities() {
		return this.abilities;
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
		for (int i = 0; i < abilities.length; i++) {
			final String ability = abilities[i];
			final int slot = i + 1;
			new BukkitRunnable() {
				PreparedStatement ps;

				@Override
				public void run() {
					try {
						this.ps = DBConnection.sql.getConnection().prepareStatement(updateQuery1 + slot + updateQuery2);
						this.ps.setString(1, ability);
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
