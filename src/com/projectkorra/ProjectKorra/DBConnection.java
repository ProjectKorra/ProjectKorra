package com.projectkorra.ProjectKorra;

import com.projectkorra.ProjectKorra.Storage.Database;
import com.projectkorra.ProjectKorra.Storage.MySQL;
import com.projectkorra.ProjectKorra.Storage.SQLite;

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
			sql = new MySQL(ProjectKorra.log, "[ProjectKorra] Establishing MySQL Connection...", host, port, user, pass, db);
			if (((MySQL) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}

			isOpen = true;
			ProjectKorra.log.info("[ProjectKorra] Database connection established.");

			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table");
				String query = "CREATE TABLE `pk_players` (" + "`id` int(32) NOT NULL AUTO_INCREMENT," + "`uuid` varchar(255)," + "`player` varchar(255)," + "`element` varchar(255)," + "`permaremoved` varchar(5)," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				String query = "CREATE TABLE `pk_presets` (" + "`id` int(32) NOT NULL AUTO_INCREMENT," + "`uuid` varchar(255)," + "`name` varchar(255)," + "`slot1` varchar(255)," + "`slot2` varchar(255)," + "`slot3` varchar(255)," + "`slot4` varchar(255)," + "`slot5` varchar(255)," + "`slot6` varchar(255)," + "`slot7` varchar(255)," + "`slot8` varchar(255)," + "`slot9` varchar(255)," + " PRIMARY KEY (id));";
				sql.modifyQuery(query);
			}
		} else {
			sql = new SQLite(ProjectKorra.log, "[ProjectKorra] Establishing SQLite Connection.", "projectkorra.db", ProjectKorra.plugin.getDataFolder().getAbsolutePath());
			if (((SQLite) sql).open() == null) {
				ProjectKorra.log.severe("Disabling due to database error");
				GeneralMethods.stopPlugin();
				return;
			}

			isOpen = true;
			if (!sql.tableExists("pk_players")) {
				ProjectKorra.log.info("Creating pk_players table.");
				String query = "CREATE TABLE `pk_players` (" + "`id` INTEGER PRIMARY KEY," + "`uuid` TEXT(255)," + "`player` TEXT(255)," + "`element` TEXT(255)," + "`permaremoved` TEXT(5)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255));";
				sql.modifyQuery(query);
			}

			if (!sql.tableExists("pk_presets")) {
				ProjectKorra.log.info("Creating pk_presets table");
				String query = "CREATE TABLE `pk_presets` (" + "`id` INTEGER PRIMARY KEY," + "`uuid` TEXT(255)," + "`name` TEXT(255)," + "`slot1` TEXT(255)," + "`slot2` TEXT(255)," + "`slot3` TEXT(255)," + "`slot4` TEXT(255)," + "`slot5` TEXT(255)," + "`slot6` TEXT(255)," + "`slot7` TEXT(255)," + "`slot8` TEXT(255)," + "`slot9` TEXT(255));";
				sql.modifyQuery(query);
			}
		}
	}

	public static boolean isOpen() {
		return isOpen;
	}
}