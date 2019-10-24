package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.configuration.Config;

public class DatabaseConfig implements Config {

	public final DatabaseManager.Engine Engine = DatabaseManager.Engine.SQLITE;

	public final String SQLite_File = "projectkorra.sql";

	public final String MySQL_IP = "localhost";
	public final String MySQL_Port = "3306";
	public final String MySQL_DatabaseName = "projectkorra";
	public final String MySQL_Username = "root";
	public final String MySQL_Password = "password";

	@Override
	public String getName() {
		return "Database";
	}

	@Override
	public String[] getParents() {
		return new String[0];
	}
}
