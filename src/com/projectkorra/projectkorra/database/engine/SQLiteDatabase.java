package com.projectkorra.projectkorra.database.engine;

import com.projectkorra.projectkorra.database.DatabaseConfig;
import com.projectkorra.projectkorra.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements SQLDatabase {

	private final File databaseFile;
	private Connection connection;

	public SQLiteDatabase(DatabaseManager databaseManager, DatabaseConfig databaseConfig) {
		this.databaseFile = new File(databaseManager.getPlugin().getDataFolder(), databaseConfig.SQLite_File);

		if (!this.databaseFile.getParentFile().exists()) {
			this.databaseFile.getParentFile().mkdirs();
		}

		if (!this.databaseFile.exists()) {
			try {
				this.databaseFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		open();
	}

	public void open() {
		try {
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection getConnection() {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				open();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return this.connection;
	}

	@Override
	public void close() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
