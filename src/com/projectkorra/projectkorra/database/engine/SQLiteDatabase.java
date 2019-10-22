package com.projectkorra.projectkorra.database.engine;

import com.projectkorra.projectkorra.database.DatabaseConfig;
import com.projectkorra.projectkorra.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements SQLDatabase
{
	private final File _databaseFile;
	private Connection _connection;

	public SQLiteDatabase(DatabaseManager databaseManager, DatabaseConfig databaseConfig)
	{
		_databaseFile = new File(databaseManager.getPlugin().getDataFolder(), databaseConfig.SQLite_File);

		if (!_databaseFile.getParentFile().exists())
		{
			_databaseFile.getParentFile().mkdirs();
		}

		if (!_databaseFile.exists())
		{
			try
			{
				_databaseFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		open();
	}

	public void open()
	{
		try
		{
			_connection = DriverManager.getConnection("jdbc:sqlite:" + _databaseFile.getAbsolutePath());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Connection getConnection()
	{
		try
		{
			if (_connection == null || _connection.isClosed())
			{
				open();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return _connection;
	}

	@Override
	public void close()
	{
		try
		{
			_connection.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
