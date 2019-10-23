package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.database.DatabaseQuery;
import com.projectkorra.projectkorra.database.DatabaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AbilityRepository extends DatabaseRepository
{
	private static final DatabaseQuery CREATE_TABLE_PLAYER_ABILITIES = DatabaseQuery.newBuilder()
			.mysql("CREATE TABLE IF NOT EXISTS pk_player_abilities (player_id INTEGER REFERENCES pk_bending_players (player_id), ability_name VARCHAR(50) NOT NULL, slot TINYINT NOT NULL, PRIMARY KEY (player_id, ability_name), INDEX player_index (player_id), INDEX ability_index (ability_name));")
			.sqlite("CREATE TABLE IF NOT EXISTS pk_player_abilities (player_id INTEGER REFERENCES pk_bending_players (player_id), ability_name VARCHAR(50) NOT NULL, slot TINYINT NOT NULL, PRIMARY KEY (player_id, ability_name)); CREATE INDEX player_index ON pk_player_abilities (player_id); CREATE INDEX ability_index ON pk_player_abilities (ability_name);")
			.build();

	private static final DatabaseQuery SELECT_PLAYER_ABILITIES = DatabaseQuery.newBuilder()
			.query("SELECT ability_name, slot FROM pk_player_abilities WHERE player_id = ?;")
			.build();

	private static final DatabaseQuery INSERT_PLAYER_ABILITY = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_player_abilities VALUES (?, ?, ?);")
			.build();

	private static final DatabaseQuery DELETE_PLAYER_ABILITIES = DatabaseQuery.newBuilder()
			.query("DELETE FROM pk_player_abilities WHERE player_id = ?")
			.build();

	private static final DatabaseQuery DELETE_PLAYER_ABILITY = DatabaseQuery.newBuilder()
			.query("DELETE FROM pk_player_abilities WHERE player_id = ? AND ability_name = ?;")
			.build();

	protected void createTables() throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_PLAYER_ABILITIES.getQuery()))
		{
			statement.executeUpdate();
		}
	}

	protected String[] selectPlayerAbilities(int playerId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_ABILITIES.getQuery()))
		{
			statement.setInt(1, playerId);

			String[] abilities = new String[9];

			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					String abilityName = rs.getString("ability_name");
					int slot = rs.getInt("slot");

					if (slot < 0 || slot >= abilities.length)
					{
						// TODO Log illegal slot
						continue;
					}

					abilities[slot] = abilityName;
				}

				return abilities;
			}
		}
	}

	protected void insertPlayerAbility(int playerId, String abilityName, int slot) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER_ABILITY.getQuery()))
		{
			statement.setInt(1, playerId);
			statement.setString(2, abilityName);
			statement.setInt(3, slot);

			statement.executeUpdate();
		}
	}

	protected void deletePlayerAbilities(int playerId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_ABILITIES.getQuery()))
		{
			statement.setInt(1, playerId);

			statement.executeUpdate();
		}
	}

	protected void deletePlayerAbility(int playerId, String abilityName) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_ABILITY.getQuery()))
		{
			statement.setInt(1, playerId);
			statement.setString(2, abilityName);

			statement.executeUpdate();
		}
	}
}
