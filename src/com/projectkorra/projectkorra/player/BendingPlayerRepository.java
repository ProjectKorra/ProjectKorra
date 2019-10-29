package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.database.DatabaseQuery;
import com.projectkorra.projectkorra.database.DatabaseRepository;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

public class BendingPlayerRepository extends DatabaseRepository {

	private static final DatabaseQuery CREATE_TABLE_BENDING_PLAYERS = DatabaseQuery.newBuilder()
			.mysql("CREATE TABLE IF NOT EXISTS pk_bending_players (player_id INTEGER PRIMARY KEY AUTO_INCREMENT, uuid BINARY(16) NOT NULL, player_name VARCHAR(16) NOT NULL, first_login BIGINT NOT NULL, bending_permanently_removed BOOLEAN, INDEX uuid_index (uuid));")
			.sqlite("CREATE TABLE IF NOT EXISTS pk_bending_players (player_id INTEGER PRIMARY KEY AUTOINCREMENT, uuid BINARY(16) NOT NULL, player_name VARCHAR(16) NOT NULL, first_login BIGINT NOT NULL, bending_permanently_removed BOOLEAN); CREATE INDEX uuid_index ON pk_bending_players (uuid);")
			.build();

	private static final DatabaseQuery SELECT_BENDING_PLAYER = DatabaseQuery.newBuilder()
			.query("SELECT player_id, player_name, first_login, bending_removed FROM pk_bending_players WHERE uuid = ?;")
			.build();

	private static final DatabaseQuery INSERT_BENDING_PLAYER = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_bending_players (uuid, player_name, first_login) VALUES (?, ?, ?);")
			.build();

	private static final DatabaseQuery UPDATE_PLAYER_NAME = DatabaseQuery.newBuilder()
			.query("UPDATE pk_bending_players SET player_name = ? WHERE player_id = ?;")
			.build();

	private static final DatabaseQuery UPDATE_BENDING_PERMANENTLY_REMOVED = DatabaseQuery.newBuilder()
			.query("UPDATE pk_bending_players SET bending_permanently_removed = ? WHERE player_id = ?;")
			.build();

	protected void createTables() throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_BENDING_PLAYERS.getQuery())) {
			statement.executeUpdate();
		}
	}

	protected BendingPlayer selectPlayer(Player player) throws SQLException {
		UUID uuid = player.getUniqueId();
		byte[] binaryUUID = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();

		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_BENDING_PLAYER.getQuery())) {
			statement.setBytes(1, binaryUUID);

			try (ResultSet rs = statement.executeQuery()) {
				if (!rs.next()) {
					return insertPlayer(player.getUniqueId(), player.getName());
				}

				int playerId = rs.getInt("player_id");
				String playerName = rs.getString("player_name");
				long firstLogin = rs.getLong("first_login");
				boolean bendingPermanentlyRemoved = rs.getBoolean("bending_permanently_removed");

				if (!player.getName().equals(playerName)) {
					updatePlayerName(playerId, player.getName());
				}

				BendingPlayer bendingPlayer = new BendingPlayer(playerId, uuid, playerName, firstLogin);

				bendingPlayer.setBendingPermanentlyRemoved(bendingPermanentlyRemoved);

				return bendingPlayer;
			}
		}
	}

	private BendingPlayer insertPlayer(UUID uuid, String playerName) throws SQLException {
		byte[] binaryUUID = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();

		Connection connection = getDatabase().getConnection();
		long firstLogin = System.currentTimeMillis();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_BENDING_PLAYER.getQuery(), Statement.RETURN_GENERATED_KEYS)) {
			statement.setBytes(1, binaryUUID);
			statement.setString(2, playerName);
			statement.setLong(3, firstLogin);

			statement.executeUpdate();

			try (ResultSet rs = statement.getGeneratedKeys()) {
				rs.next();

				int playerId = rs.getInt(1);

				return new BendingPlayer(playerId, uuid, playerName, firstLogin);
			}
		}
	}

	protected void updatePlayerName(int playerId, String playerName) throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(UPDATE_PLAYER_NAME.getQuery())) {
			statement.setInt(1, playerId);
			statement.setString(2, playerName);

			statement.executeUpdate();
		}
	}

	protected void updateBendingRemoved(BendingPlayer bendingPlayer) throws SQLException {
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(UPDATE_BENDING_PERMANENTLY_REMOVED.getQuery())) {
			statement.setInt(1, bendingPlayer.getId());
			statement.setBoolean(2, bendingPlayer.isBendingPermanentlyRemoved());

			statement.executeUpdate();
		}
	}
}
