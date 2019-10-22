package com.projectkorra.projectkorra.database.engine;

import com.projectkorra.projectkorra.database.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase implements SQLDatabase
{
	private final HikariDataSource _hikari;

	public MySQLDatabase(DatabaseConfig databaseConfig)
	{
		HikariConfig hikariConfig = new HikariConfig();

		hikariConfig.setJdbcUrl("jdbc:mysql://" + databaseConfig.MySQL_IP + ":" + databaseConfig.MySQL_Port + "/" + databaseConfig.MySQL_DatabaseName);
		hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
		hikariConfig.setUsername(databaseConfig.MySQL_Username);
		hikariConfig.setPassword(databaseConfig.MySQL_Password);
		hikariConfig.setMinimumIdle(1);
		hikariConfig.setMaximumPoolSize(10);
		hikariConfig.setConnectionTimeout(10000);

		_hikari = new HikariDataSource(hikariConfig);
	}

	@Override
	public Connection getConnection()
	{
		try (Connection connection = _hikari.getConnection())
		{
			return connection;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void close()
	{
		_hikari.close();
	}
}
