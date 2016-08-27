package com.projectkorra.projectkorra.storage;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

public class DBConnection {

	public static Database sql;

	public static String host;
	public static int port;
	public static String db;
	public static String user;
	public static String pass;
	public static boolean isOpen = false;
	
	public static void init() {
		if (ProjectKorra.plugin.getConfig().getString("Storage.engine").equalsIgnoreCase("mysql")) {
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
				String query = "CREATE TABLE `pk_players` (" + "`uuid` varchar(36) NOT NULL," + "`player` varchar(16) NOT NULL," + "`element` varchar(255)," + "`subelement` varchar(255)," + "`permaremoved` varchar(5)," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (uuid));";
				sql.modifyQuery(query, false);
			} else {				
				try {
					DatabaseMetaData md = sql.connection.getMetaData();
					if (!md.getColumns(null, null, "pk_players", "subelement").next()) {
						ProjectKorra.log.info("Updating Database with subelements...");
						sql.getConnection().setAutoCommit(false);
						sql.modifyQuery("ALTER TABLE `pk_players` ADD subelement varchar(255);", false);
						sql.getConnection().commit();
						sql.modifyQuery("UPDATE pk_players SET subelement = '-';", false);
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				String query = "CREATE TABLE `pk_presets` (" + "`uuid` varchar(36) NOT NULL," + "`name` varchar(255) NOT NULL," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (uuid, name));";
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
				String query = "CREATE TABLE `pk_players` (" + "`uuid` TEXT(36) PRIMARY KEY," + "`player` TEXT(16)," + "`element` TEXT(255)," + "`subelement` TEXT(255)," + "`permaremoved` TEXT(5)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255));";
				sql.modifyQuery(query, false);
			} else {
				try {
					DatabaseMetaData md = sql.connection.getMetaData();
					if (!md.getColumns(null, null, "pk_players", "subelement").next()) {						
						ProjectKorra.log.info("Updating Database with subelements...");
						sql.getConnection().setAutoCommit(false);
						sql.modifyQuery("ALTER TABLE `pk_players` ADD subelement TEXT(255);", false);
						sql.getConnection().commit();
						sql.modifyQuery("UPDATE pk_players SET subelement = '-';", false);
						sql.getConnection().setAutoCommit(true);
						ProjectKorra.log.info("Database Updated.");
					}
						
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				String query = "CREATE TABLE `pk_presets` (" + "`uuid` TEXT(36)," + "`name` TEXT(255)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255)," + "PRIMARY KEY (uuid, name));";
				sql.modifyQuery(query, false);
			}
		}
	}

	public static boolean isOpen() {
		return isOpen;
	}
}
