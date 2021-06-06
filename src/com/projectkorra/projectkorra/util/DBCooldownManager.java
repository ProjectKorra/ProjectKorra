package com.projectkorra.projectkorra.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.storage.MySQL;

public class DBCooldownManager extends Manager {

	private DBCooldownManager() {}

	@Override
	public void onActivate() {
		this.setupCooldowns();
	}

	public void setupCooldowns() {
		// Create pk_cooldown_ids table.
		if (!DBConnection.sql.tableExists("pk_cooldown_ids")) {
			ProjectKorra.log.info("Creating pk_cooldown_ids table");
			String query = "CREATE TABLE `pk_cooldown_ids` (id INTEGER PRIMARY KEY AUTOINCREMENT, cooldown_name TEXT(256) NOT NULL);";
			if (DBConnection.sql instanceof MySQL) {
				query = "CREATE TABLE `pk_cooldown_ids` (id INTEGER PRIMARY KEY AUTO_INCREMENT, cooldown_name VARCHAR(256) NOT NULL);";
			}
			DBConnection.sql.modifyQuery(query, false);
		}
		// Create pk_cooldowns table.
		if (!DBConnection.sql.tableExists("pk_cooldowns")) {
			ProjectKorra.log.info("Creating pk_cooldowns table");
			String query = "CREATE TABLE `pk_cooldowns` (uuid TEXT(36) NOT NULL, cooldown_id INTEGER NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown_id));";
			if (DBConnection.sql instanceof MySQL) {
				query = "CREATE TABLE `pk_cooldowns` (uuid VARCHAR(36) NOT NULL, cooldown_id INTEGER NOT NULL, value BIGINT, PRIMARY KEY (uuid, cooldown_id));";
			}
			DBConnection.sql.modifyQuery(query, false);
		}
	}

	public int getCooldownId(final String cooldown, final boolean async) {
		int id = -1;
		Statement stmt = null;
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT id FROM pk_cooldown_ids WHERE cooldown_name = '" + cooldown + "'")) {
			if (rs.next()) {
				id = rs.getInt("id");
			} else {
				DBConnection.sql.modifyQuery("INSERT INTO pk_cooldown_ids (cooldown_name) VALUES ('" + cooldown + "')", async);
				id = this.getCooldownId(cooldown, async);
			}
			stmt = rs.getStatement();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	public String getCooldownName(final int id) {
		String name = "";
		Statement stmt = null;
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT cooldown_name FROM pk_cooldown_ids WHERE id = " + id)) {
			if (rs.next()) {
				name = rs.getString("cooldown_name");
			}
			stmt = rs.getStatement();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		return name;
	}

}
