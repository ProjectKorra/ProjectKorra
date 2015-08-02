package com.projectkorra.projectkorra.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SQLite extends Database {
	private String location;
	private String database;
	private File SQLfile;

	public SQLite(Logger log, String prefix, String database, String location) {
		super(log, prefix, "[SQLite] ");
		this.database = database;
		this.location = location;

		File folder = new File(this.location);

		if (!folder.exists()) {
			folder.mkdirs();
		}

		this.SQLfile = new File(folder.getAbsolutePath() + File.separator + this.database);
	}

	@Override
	public Connection open() {
		try {
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.SQLfile.getAbsolutePath());
			this.printInfo("Connection established!");

			return this.connection;
		}
		catch (ClassNotFoundException e) {
			this.printErr("JDBC driver not found!", true);
			return null;
		}
		catch (SQLException e) {
			this.printErr("SQLite exception during connection.", true);
			return null;
		}
	}

}
