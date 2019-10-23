package com.projectkorra.projectkorra.element;

import com.projectkorra.projectkorra.database.DatabaseQuery;
import com.projectkorra.projectkorra.database.DatabaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class ElementRepository extends DatabaseRepository
{
	private static final DatabaseQuery CREATE_TABLE_ELEMENTS = DatabaseQuery.newBuilder()
			.mysql("CREATE TABLE IF NOT EXISTS pk_elements (element_id INTEGER PRIMARY KEY AUTO_INCREMENT, element_name VARCHAR(50) NOT NULL, UNIQUE INDEX name_index (element_name));")
			.sqlite("CREATE TABLE IF NOT EXISTS pk_elements (element_id INTEGER PRIMARY KEY AUTOINCREMENT, element_name VARCHAR(50) NOT NULL); CREATE UNIQUE INDEX name_index ON pk_elements (element_name);")
			.build();
	private static final DatabaseQuery CREATE_TABLE_PLAYER_ELEMENTS = DatabaseQuery.newBuilder()
			.mysql("CREATE TABLE IF NOT EXISTS pk_player_elements (player_id INTEGER REFERENCES pk_bending_players (player_id), element_id INTEGER REFERENCES pk_elements (element_id), PRIMARY KEY (player_id, element_id), INDEX player_index (player_id), INDEX element_index (element_id));")
			.sqlite("CREATE TABLE IF NOT EXISTS pk_player_elements (player_id INTEGER REFERENCES pk_bending_players (player_id), element_id INTEGER REFERENCES pk_elements (element_id), PRIMARY KEY (player_id, element_id)); CREATE INDEX player_index ON pk_player_elements (player_id); CREATE INDEX element_index ON pk_player_elements (element_id);")
			.build();

	private static final DatabaseQuery SELECT_ELEMENT_ID = DatabaseQuery.newBuilder()
			.query("SELECT element_id FROM pk_elements WHERE element_name = ?;")
			.build();

	private static final DatabaseQuery INSERT_ELEMENT_ID = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_elements (element_name) VALUES (?);")
			.build();

	private static final DatabaseQuery SELECT_PLAYER_ELEMENTS = DatabaseQuery.newBuilder()
			.query("SELECT element_id FROM pk_player_elements WHERE player_id = ?;")
			.build();

	private static final DatabaseQuery INSERT_PLAYER_ELEMENT = DatabaseQuery.newBuilder()
			.query("INSERT INTO pk_player_elements VALUES (?, ?);")
			.build();

	private static final DatabaseQuery DELETE_PLAYER_ELEMENTS = DatabaseQuery.newBuilder()
			.query("DELETE FROM pk_player_elements WHERE player_id = ?;")
			.build();

	private static final DatabaseQuery DELETE_PLAYER_ELEMENT = DatabaseQuery.newBuilder()
			.query("DELETE FROM pk_player_elements WHERE player_id = ? AND element_id = ?;")
			.build();

	protected void createTables() throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try
				(
						PreparedStatement elements = connection.prepareStatement(CREATE_TABLE_ELEMENTS.getQuery());
						PreparedStatement playerElements = connection.prepareStatement(CREATE_TABLE_PLAYER_ELEMENTS.getQuery())
				)
		{
			elements.executeUpdate();
			playerElements.executeUpdate();
		}
	}

	protected int selectElemenetId(String elementName) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_ELEMENT_ID.getQuery()))
		{
			statement.setString(1, elementName);

			try (ResultSet rs = statement.executeQuery())
			{
				if (!rs.next())
				{
					return insertElementId(elementName);
				}

				return rs.getInt("element_id");
			}
		}
	}

	private int insertElementId(String elementName) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_ELEMENT_ID.getQuery(), Statement.RETURN_GENERATED_KEYS))
		{
			statement.setString(1, elementName);

			statement.executeUpdate();

			try (ResultSet rs = statement.getGeneratedKeys())
			{
				rs.next();

				return rs.getInt(1);
			}
		}
	}

	protected Set<Integer> selectPlayerElements(int playerId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_ELEMENTS.getQuery()))
		{
			statement.setInt(1, playerId);

			Set<Integer> elements = new HashSet<>();

			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					elements.add(rs.getInt("element_id"));
				}

				return elements;
			}
		}
	}

	protected void insertPlayerElement(int playerId, int elementId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER_ELEMENT.getQuery()))
		{
			statement.setInt(1, playerId);
			statement.setInt(2, elementId);

			statement.executeUpdate();
		}
	}

	protected void deletePlayerElements(int playerId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_ELEMENTS.getQuery()))
		{
			statement.setInt(1, playerId);

			statement.executeUpdate();
		}
	}

	protected void deletePlayerElement(int playerId, int elementId) throws SQLException
	{
		Connection connection = getDatabase().getConnection();

		try (PreparedStatement statement = connection.prepareStatement(DELETE_PLAYER_ELEMENT.getQuery()))
		{
			statement.setInt(1, playerId);
			statement.setInt(2, elementId);

			statement.executeUpdate();
		}
	}
}
