package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.database.engine.MySQLDatabase;
import com.projectkorra.projectkorra.database.engine.SQLDatabase;
import com.projectkorra.projectkorra.database.engine.SQLiteDatabase;
import com.projectkorra.projectkorra.module.Module;

import java.util.logging.Level;

public class DatabaseManager extends Module {

	private final DatabaseConfig config;
	private final SQLDatabase database;

	private DatabaseManager() {
		super("Database");

		this.config = ConfigManager.getConfig(DatabaseConfig.class);

		switch (this.config.Engine) {
			case MYSQL:
				this.database = new MySQLDatabase(this.config);
				break;
			case SQLITE:
				this.database = new SQLiteDatabase(this, this.config);
				break;
			default:
				log(Level.SEVERE, "Unknown database engine.");
				this.database = null;
				break;
		}
	}

	public DatabaseConfig getConfig() {
		return this.config;
	}

	public SQLDatabase getDatabase() {
		return this.database;
	}

	@Override
	public void onDisable() {
		this.database.close();
	}

	public enum Engine {
		MYSQL,
		SQLITE;
	}
}
