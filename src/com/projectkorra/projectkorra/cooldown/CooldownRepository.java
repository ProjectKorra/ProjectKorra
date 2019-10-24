package com.projectkorra.projectkorra.cooldown;

import com.projectkorra.projectkorra.database.DatabaseQuery;
import com.projectkorra.projectkorra.database.DatabaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CooldownRepository extends DatabaseRepository {

	private static final DatabaseQuery CREATE_TABLE_COOLDOWNS = DatabaseQuery.newBuilder()
			.query("CREATE TABLE IF NOT EXISTS pk_cooldowns (player_id INTEGER REFERENCES pk_bending_players (player_id), ability_name VARCHAR(100) NOT NULL, expire_time BIGINT NOT NULL, PRIMARY KEY (player_id, ability_name));")
			.build();

	private static final DatabaseQuery SELECT_COOLDOWNS = DatabaseQuery.newBuilder()
			.query("SELECT * FROM pk_cooldowns WHERE player_id = ?")
			.build();

	private static final DatabaseQuery INSERT_COOLDOWN = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_cooldowns VALUES (?, ?, ?);")
			.build();

	private static final DatabaseQuery DELETE_COOLDOWN = DatabaseQuery.newBuilder()
			.query("DELETE FROM pk_cooldowns WHERE player_id = ? AND ability_name = ?;")
			.build();

	protected void createTables() throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_COOLDOWNS.getQuery())) {
			statement.executeUpdate();
		}
	}

	protected Map<String, CooldownManager.Cooldown> selectCooldowns(int playerId) throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_COOLDOWNS.getQuery())) {
			statement.setInt(1, playerId);

			Map<String, CooldownManager.Cooldown> cooldowns = new HashMap<>();

			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					String abilityName = rs.getString("ability_name");
					long expireTime = rs.getLong("expire_time");

					cooldowns.put(abilityName, new CooldownManager.Cooldown(abilityName, expireTime, true));
				}

				return cooldowns;
			}
		}
	}

	protected void insertCooldown(int playerId, String abilityName, long expireTime) throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_COOLDOWN.getQuery())) {
			statement.setInt(1, playerId);
			statement.setString(2, abilityName);
			statement.setLong(3, expireTime);

			statement.executeUpdate();
		}
	}

	protected void deleteCooldown(int playerId, String abilityName) throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(DELETE_COOLDOWN.getQuery())) {
			statement.setInt(1, playerId);
			statement.setString(2, abilityName);

			statement.executeUpdate();
		}
	}
}
