package com.projectkorra.projectkorra.storage;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class DBConnection {

	public static Database sql;

	private static String host;
	private static int port;
	private static String db;
	private static String user;
	private static String pass;
	private static boolean isOpen = false;

	public static void init() {
		DBConnection.host = ConfigManager.getConfig().getString("Storage.MySQL.host");
		DBConnection.port = ConfigManager.getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = ConfigManager.getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = ConfigManager.getConfig().getString("Storage.MySQL.db");
		DBConnection.user = ConfigManager.getConfig().getString("Storage.MySQL.user");

		if (ProjectKorra.plugin.getConfig().getString("Storage.engine").equalsIgnoreCase("mysql")) {
			sql = new MySQL(ProjectKorra.log, host, port, user, pass, db);
			if (((MySQL) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}
			isOpen = true;
			ProjectKorra.log.info("Database connection established.");

			convertOldCooldownsTable();

			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table");
				final String query = "CREATE TABLE `pk_players` (" + "`uuid` varchar(36) NOT NULL," + "`player` varchar(16) NOT NULL," + "`element` varchar(255)," + "`subelement` varchar(255)," + "`permaremoved` varchar(5)," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (uuid));";
				sql.modifyQuery(query, false);
			} else {
				try {
					final DatabaseMetaData md = sql.connection.getMetaData();
					if (!md.getColumns(null, null, "pk_players", "subelement").next()) {
						ProjectKorra.log.info("Updating Database with subelements...");
						sql.getConnection().setAutoCommit(false);
						sql.modifyQuery("ALTER TABLE `pk_players` ADD subelement varchar(255);", false);
						sql.getConnection().commit();
						sql.modifyQuery("UPDATE pk_players SET subelement = '-';", false);
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				final String query = "CREATE TABLE `pk_presets` (" + "`uuid` varchar(36) NOT NULL," + "`name` varchar(255) NOT NULL," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (uuid, name));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldowns")) {
				ProjectKorra.log.info("Creating pk_cooldowns table");
				final String query = "CREATE TABLE `pk_cooldowns` (uuid VARCHAR(36) NOT NULL, cooldown VARCHAR(255) NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_board")) {
				ProjectKorra.log.info("Creating pk_board table");
				final String query = "CREATE TABLE `pk_board` (uuid VARCHAR(36) NOT NULL, enabled BOOLEAN NOT NULL, PRIMARY KEY (uuid));";
				sql.modifyQuery(query, false);
			}
		} else {
			sql = new SQLite(ProjectKorra.log, "projectkorra.db", ProjectKorra.plugin.getDataFolder().getAbsolutePath());
			if (((SQLite) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}
			isOpen = true;

			convertOldCooldownsTable();

			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table.");
				final String query = "CREATE TABLE `pk_players` (" + "`uuid` TEXT(36) PRIMARY KEY," + "`player` TEXT(16)," + "`element` TEXT(255)," + "`subelement` TEXT(255)," + "`permaremoved` TEXT(5)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255));";
				sql.modifyQuery(query, false);
			} else {
				try {
					final DatabaseMetaData md = sql.connection.getMetaData();
					if (!md.getColumns(null, null, "pk_players", "subelement").next()) {
						ProjectKorra.log.info("Updating Database with subelements...");
						sql.getConnection().setAutoCommit(false);
						sql.modifyQuery("ALTER TABLE `pk_players` ADD subelement TEXT(255);", false);
						sql.getConnection().commit();
						sql.modifyQuery("UPDATE pk_players SET subelement = '-';", false);
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}

				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				final String query = "CREATE TABLE `pk_presets` (" + "`uuid` TEXT(36)," + "`name` TEXT(255)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255)," + "PRIMARY KEY (uuid, name));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldowns")) {
				ProjectKorra.log.info("Creating pk_cooldowns table");
				final String query = "CREATE TABLE `pk_cooldowns` (uuid TEXT(36) NOT NULL, cooldown TEXT(255) NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_board")) {
				ProjectKorra.log.info("Creating pk_board table");
				final String query = "CREATE TABLE `pk_board` (uuid TEXT(36) NOT NULL, enabled INTEGER NOT NULL, PRIMARY KEY (uuid));";
				sql.modifyQuery(query, false);
			}
		}
	}

	/**
	 * Converts the old cooldowns table to one that doesn't use IDs. IDs are slow and pointless
	 */
	private static void convertOldCooldownsTable() {
		if (DBConnection.sql.tableExists("pk_cooldown_ids")) {

			Map<Integer, String> oldCooldownIDs = new HashMap<>();
			Map<String, Map<String, Long>> oldTable = new HashMap<>();

			// Get all cooldown ids from the database.
			try {
				ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_cooldown_ids");
				while (rs.next()) {
					oldCooldownIDs.put(rs.getInt("id"), rs.getString("cooldown_name"));
				}
				rs.close();
			} catch (final SQLException e) {
				ProjectKorra.log.warning("Failed to get cooldown ids from database.");
				e.printStackTrace();
			}

			//Get all player cooldowns from the database
			try {
				ResultSet rs = sql.readQuery("SELECT * FROM pk_cooldowns");

				while (rs.next()) {
					final String uuid = rs.getString("uuid");
					final int cooldownID = rs.getInt("cooldown_id");
					final long cooldown = rs.getLong("value");

					// If uuid is not in the oldTable, add it.
					if (!oldTable.containsKey(uuid)) oldTable.put(uuid, new HashMap<>());

					String cooldownName = oldCooldownIDs.get(cooldownID);

					if (cooldownName == null || cooldownName.equals("")) {
						ProjectKorra.log.warning("Failed to get cooldown name from database.");
						continue;
					}

					oldTable.get(uuid).put(cooldownName, cooldown);
				}

				rs.close();
				sql.close();

				ProjectKorra.log.info("Converting old cooldowns to new cooldowns table... The DB will reconnect a few times.");

				sql.open(); // Reconnect to the database.

				// Delete old cooldowns table.
				sql.modifyQuery("DROP TABLE pk_cooldowns", false);
				sql.modifyQuery("DROP TABLE pk_cooldown_ids", false);

				sql.close(); // Close the connection again
				sql.open(); // Reconnect to the database.

				// Create new cooldowns table.
				String query = "CREATE TABLE `pk_cooldowns` (uuid TEXT(36) NOT NULL, cooldown TEXT(255) NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (uuid, cooldown));";
				if (sql instanceof MySQL) {
					query = "CREATE TABLE `pk_cooldowns` (uuid VARCHAR(36) NOT NULL, cooldown VARCHAR(255) NOT NULL, value BIGINT NOT NULL, PRIMARY KEY (uuid, cooldown));";
				}
				sql.modifyQuery(query, false);

				// Insert all cooldowns into the new table.
				for (final String uuid : oldTable.keySet()) {
					for (final String cooldown : oldTable.get(uuid).keySet()) {
						final long cooldownTime = oldTable.get(uuid).get(cooldown);
						DBConnection.sql.modifyQuery("INSERT INTO pk_cooldowns (uuid, cooldown, value) VALUES ('" + uuid + "', '" + cooldown + "', " + cooldownTime + ")", false);
					}
				}
				sql.getConnection().setAutoCommit(true);
				ProjectKorra.log.info("Finished converting old cooldowns to new cooldowns table!");
			} catch (final SQLException e) {
				ProjectKorra.log.warning("Failed to get cooldowns from database.");
				e.printStackTrace();
			}
		}
	}

	public static boolean isOpen() {
		return isOpen;
	}
}
