package com.projectkorra.projectkorra.storage;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;

public class DBConnection {

	public static Database sql;

	private static String host;
	private static int port;
	private static String db;
	private static String user;
	private static String pass;
	private static boolean isOpen = false;

	public static void init() {
		GeneralPropertiesConfig config = ConfigManager.getConfig(GeneralPropertiesConfig.class);
		DBConnection.host = config.MySQL.Host;
		DBConnection.port = config.MySQL.Port;
		DBConnection.pass = config.MySQL.Password;
		DBConnection.db = config.MySQL.Database;
		DBConnection.user = config.MySQL.Username;
		if (config.MySQL.Enabled) {
			sql = new MySQL(ProjectKorra.log, "Establishing MySQL Connection...", host, port, user, pass, db);
			if (((MySQL) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}
			isOpen = true;
			ProjectKorra.log.info("Database connection established.");
			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table");
				final String query = "CREATE TABLE `pk_players` (`uuid` varchar(36) NOT NULL, `player` varchar(16) NOT NULL, `permaremoved` bool, `slot1` varchar(255), `slot2` varchar(255), `slot3` varchar(255), `slot4` varchar(255), `slot5` varchar(255), `slot6` varchar(255), `slot7` varchar(255), `slot8` varchar(255), `slot9` varchar(255), PRIMARY KEY (uuid));";
				sql.modifyQuery(query, false);
			} else {
				try {
					final DatabaseMetaData md = sql.connection.getMetaData();
					boolean elementColumn = md.getColumns(null, null, "pk_players", "element").next();
					boolean subElementColumn = md.getColumns(null, null, "pk_players", "subelement").next();
					boolean permaremoveTextColumn = false;
					
					{
						ResultSet pr = md.getColumns(null, null, "pk_players", "permaremoved");
						if (pr.next()) {
							String type = pr.getString("TYPE_NAME");
							
							if (type.toLowerCase().contains("varchar")) {
								permaremoveTextColumn = true;
							}
						}
					}
					
					if (elementColumn || subElementColumn || permaremoveTextColumn) {
						ProjectKorra.log.info("Updating Database...");
						sql.getConnection().setAutoCommit(false);
						if (elementColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP element;", false);
						}
						if (subElementColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP subelement;", false);
						}
						if (permaremoveTextColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP permaremoved;", false);
							sql.modifyQuery("ALTER TABLE `pk_players` ADD permaremoved BOOL AFTER player;", false);
						}
						sql.getConnection().commit();
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			if (!sql.tableExists("pk_player_elements")) {
				ProjectKorra.log.info("Creating pk_player_elements table");
				final String query = "CREATE TABLE `pk_player_elements` (`uuid` varchar(36) NOT NULL, `element` varchar(36) NOT NULL, `sub_element` bool NOT NULL, PRIMARY KEY (`uuid`, `element`));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				final String query = "CREATE TABLE `pk_presets` (`uuid` varchar(36) NOT NULL, `name` varchar(255) NOT NULL, `slot1` varchar(255), `slot2` varchar(255), `slot3` varchar(255), `slot4` varchar(255), `slot5` varchar(255), `slot6` varchar(255), `slot7` varchar(255), `slot8` varchar(255), `slot9` varchar(255), PRIMARY KEY (uuid, name));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldown_ids")) {
				ProjectKorra.log.info("Creating pk_cooldown_ids table");
				final String query = "CREATE TABLE `pk_cooldown_ids` (id INTEGER PRIMARY KEY AUTO_INCREMENT, cooldown_name VARCHAR(256) NOT NULL);";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldowns")) {
				ProjectKorra.log.info("Creating pk_cooldowns table");
				final String query = "CREATE TABLE `pk_cooldowns` (uuid VARCHAR(36) NOT NULL, cooldown_id INTEGER NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown_id));";
				sql.modifyQuery(query, false);
			}
		} else {
			sql = new SQLite(ProjectKorra.log, "Establishing SQLite Connection.", "projectkorra.db", ProjectKorra.plugin.getDataFolder().getAbsolutePath());
			if (((SQLite) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}
			isOpen = true;
			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table.");
				final String query = "CREATE TABLE `pk_players` (`uuid` TEXT(36) PRIMARY KEY, `player` TEXT(16), `permaremoved` INTEGER(1), `slot1` TEXT(255), `slot2` TEXT(255), `slot3` TEXT(255), `slot4` TEXT(255), `slot5` TEXT(255), `slot6` TEXT(255), `slot7` TEXT(255), `slot8` TEXT(255), `slot9` TEXT(255));";
				sql.modifyQuery(query, false);
			} else {
				try {
					final DatabaseMetaData md = sql.connection.getMetaData();
					boolean elementColumn = md.getColumns(null, null, "pk_players", "element").next();
					boolean subElementColumn = md.getColumns(null, null, "pk_players", "subelement").next();
					boolean permaremoveTextColumn = false;
					
					{
						ResultSet pr = md.getColumns(null, null, "pk_players", "permaremoved");
						if (pr.next()) {
							String type = pr.getString("TYPE_NAME");
							
							if (type.toLowerCase().contains("text")) {
								permaremoveTextColumn = true;
							}
						}
					}
					
					if (elementColumn || subElementColumn || permaremoveTextColumn) {
						ProjectKorra.log.info("Updating Database...");
						sql.getConnection().setAutoCommit(false);
						if (elementColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP element;", false);
						}
						if (subElementColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP subelement;", false);
						}
						if (permaremoveTextColumn) {
							sql.modifyQuery("ALTER TABLE `pk_players` DROP permaremoved;", false);
							sql.modifyQuery("ALTER TABLE `pk_players` ADD permaremoved INTEGER(1) AFTER player;", false);
						}
						sql.getConnection().commit();
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			if (!sql.tableExists("pk_player_elements")) {
				ProjectKorra.log.info("Creating pk_player_elements table");
				final String query = "CREATE TABLE `pk_player_elements` (`uuid` TEXT(36) NOT NULL, `element` TEXT(36) NOT NULL, `sub_element` INTEGER(1) NOT NULL, PRIMARY KEY (`uuid`, `element`));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				final String query = "CREATE TABLE `pk_presets` (`uuid` TEXT(36), `name` TEXT(255), `slot1` TEXT(255), `slot2` TEXT(255), `slot3` TEXT(255), `slot4` TEXT(255), `slot5` TEXT(255), `slot6` TEXT(255), `slot7` TEXT(255), `slot8` TEXT(255), `slot9` TEXT(255), PRIMARY KEY (uuid, name));";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldown_ids")) {
				ProjectKorra.log.info("Creating pk_cooldown_ids table");
				final String query = "CREATE TABLE `pk_cooldown_ids` (id INTEGER PRIMARY KEY AUTOINCREMENT, cooldown_name TEXT(256) NOT NULL);";
				sql.modifyQuery(query, false);
			}
			if (!sql.tableExists("pk_cooldowns")) {
				ProjectKorra.log.info("Creating pk_cooldowns table");
				final String query = "CREATE TABLE `pk_cooldowns` (uuid TEXT(36) NOT NULL, cooldown_id INTEGER NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown_id));";
				sql.modifyQuery(query, false);
			}
		}
	}

	public static boolean isOpen() {
		return isOpen;
	}
}
