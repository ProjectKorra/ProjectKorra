package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.database.engine.SQLDatabase;
import com.projectkorra.projectkorra.module.ModuleManager;

public abstract class DatabaseRepository {

	private final DatabaseManager databaseManager;

	public DatabaseRepository() {
		this.databaseManager = ModuleManager.getModule(DatabaseManager.class);
	}

	protected SQLDatabase getDatabase() {
		return this.databaseManager.getDatabase();
	}
}
