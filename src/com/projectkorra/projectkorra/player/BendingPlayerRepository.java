package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.database.DatabaseQuery;
import com.projectkorra.projectkorra.database.DatabaseRepository;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class BendingPlayerRepository extends DatabaseRepository
{
	private static final DatabaseQuery CREATE_TABLE_BENDING_PLAYERS = DatabaseQuery.newBuilder()
			.mysql("CREATE TABLE IF NOT EXISTS pk_bending_players (player_id INTEGER PRIMARY KEY AUTO_INCREMENT, uuid BINARY(16) NOT NULL, player_name VARCHAR(16) NOT NULL, first_login BIGINT NOT NULL, INDEX uuid_index (uuid));")
			.sqlite("CREATE TABLE IF NOT EXISTS pk_bending_players (player_id INTEGER PRIMARY KEY AUTOINCREMENT, uuid BINARY(16) NOT NULL, player_name VARCHAR(16) NOT NULL, first_login BIGINT NOT NULL); CREATE INDEX uuid_index ON pk_bending_players (uuid);")
			.build();

	private static final DatabaseQuery SELECT_BENDING_PLAYER = DatabaseQuery.newBuilder()
			.query("SELECT player_id, player_name, first_login FROM pk_bending_players WHERE uuid = ?;")
			.build();

	private static final DatabaseQuery INSERT_BENDING_PLAYER = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_bending_players (uuid, player_name, first_login) VALUES (?, ?, ?);")
			.build();

	private static final DatabaseQuery UPDATE_BENDING_PLAYER = DatabaseQuery.newBuilder()
			.query("UPDATE pk_bending_players SET player_name = ? WHERE player_id = ?;")
			.build();

	protected void createTable()
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_BENDING_PLAYERS.getQuery()))
		{
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	protected BendingPlayer selectPlayer(Player player)
	{
		UUID uuid = player.getUniqueId();
		byte[] binaryUUID = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();

		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_BENDING_PLAYER.getQuery()))
		{
			statement.setBytes(1, binaryUUID);

			try (ResultSet rs = statement.executeQuery())
			{
				if (!rs.next())
				{
					return insertPlayer(player.getUniqueId(), player.getName());
				}

				int playerId = rs.getInt("player_id");
				String playerName = rs.getString("player_name");
				long firstLogin = rs.getLong("first_login");

				if (!player.getName().equals(playerName))
				{
					updatePlayer(playerId, player.getName());
				}

				return new BendingPlayer(playerId, uuid, playerName, firstLogin);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private BendingPlayer insertPlayer(UUID uuid, String playerName)
	{
		byte[] binaryUUID = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();

		Connection connection = getDatabase().getConnection();
		long firstLogin = System.currentTimeMillis();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_BENDING_PLAYER.getQuery(), Statement.RETURN_GENERATED_KEYS))
		{
			statement.setBytes(1, binaryUUID);
			statement.setString(2, playerName);
			statement.setLong(3, firstLogin);

			statement.executeUpdate();

			try (ResultSet rs = statement.getGeneratedKeys())
			{
				if (rs.next())
				{
					int playerId = rs.getInt(1);

					return new BendingPlayer(playerId, uuid, playerName, firstLogin);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	protected void updatePlayer(int playerId, String playerName)
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(UPDATE_BENDING_PLAYER.getQuery()))
		{
			statement.setInt(1, playerId);
			statement.setString(2, playerName);

			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
