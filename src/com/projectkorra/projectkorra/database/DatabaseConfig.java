package com.projectkorra.projectkorra.database;

public class DatabaseConfig
{
	public final DatabaseManager.Engine Engine = DatabaseManager.Engine.SQLITE;

	public final String SQLite_File = "projectkorra.sql";

	public final String MySQL_IP = "localhost";
	public final String MySQL_Port = "3306";
	public final String MySQL_DatabaseName = "projectkorra";
	public final String MySQL_Username = "root";
	public final String MySQL_Password = "password";
}
