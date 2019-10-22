package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.database.engine.MySQLDatabase;
import com.projectkorra.projectkorra.database.engine.SQLDatabase;
import com.projectkorra.projectkorra.database.engine.SQLiteDatabase;
import com.projectkorra.projectkorra.module.Module;

import java.util.logging.Level;

public class DatabaseManager extends Module
{
	private final DatabaseConfig _config;
	private final SQLDatabase _database;

	private DatabaseManager()
	{
		super("Database");

		// TODO Pull from new ConfigManager
		_config = new DatabaseConfig();

		switch (_config.Engine)
		{
		case MYSQL:
			_database = new MySQLDatabase(_config);
			break;
		case SQLITE:
			_database = new SQLiteDatabase(this, _config);
			break;
		default:
			log(Level.SEVERE, "Unknown database engine.");
			_database = null;
			break;
		}
	}

	public DatabaseConfig getConfig()
	{
		return _config;
	}

	public SQLDatabase getDatabase()
	{
		return _database;
	}

	@Override
	public void onDisable()
	{
		_database.close();
	}

	public enum Engine
	{
		MYSQL, SQLITE;
	}
}
