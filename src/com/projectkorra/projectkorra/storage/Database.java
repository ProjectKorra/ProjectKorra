package com.projectkorra.projectkorra.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;

public abstract class Database {

	protected final Logger log;
	protected final String dbprefix;
	protected Connection connection = null;

	public Database(final Logger log, final String dbprefix) {
		this.log = log;
		this.dbprefix = dbprefix;
	}

	/**
	 * Print information to console.
	 *
	 * @param message The string to print to console
	 */
	protected void printInfo(final String message) {
		this.log.info(this.dbprefix + message);
	}

	/**
	 * Print error to console.
	 *
	 * @param message The string to print to console
	 * @param severe If {@param severe} is true print an error, else print a
	 *            warning
	 */
	protected void printErr(final String message, final boolean severe) {
		if (severe) {
			this.log.severe(this.dbprefix + message);
		} else {
			this.log.warning(this.dbprefix + message);
		}
	}

	/**
	 * Returns the current Connection.
	 *
	 * @return Connection if exists, else null
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Opens connection to Database.
	 *
	 * @return Connection if successful
	 */
	abstract Connection open();

	/**
	 * Close connection to Database.
	 */
	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		} else {
			this.printErr("There was no SQL connection open.", false);
		}
	}

	/**
	 * Queries the Database, for queries which modify data. Run async by
	 * default.
	 *
	 * @param query Query to run
	 */
	public void modifyQuery(final String query) {
		this.modifyQuery(query, true);
	}

	/**
	 * Queries the Databases, for queries which modify data.
	 *
	 * @param query Query to run
	 * @param async If to run asynchronously
	 */
	public void modifyQuery(final String query, final boolean async) {
		if (async) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Database.this.doQuery(query);
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		} else {
			this.doQuery(query);
		}
	}

	/**
	 * Queries the Database, for queries which return results.
	 *
	 * @param query Query to run
	 * @return Result set of ran query
	 */
	public ResultSet readQuery(final String query) {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				System.out.println("Connection is closed, opening connection...+" + query);
				this.open();
			}
			final PreparedStatement stmt = this.connection.prepareStatement(query);
			final ResultSet rs = stmt.executeQuery();

			return rs;
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Check database to see if a table exists.
	 *
	 * @param table Table name to check
	 * @return true if table exists, else false
	 */
	public boolean tableExists(final String table) {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				this.open();
			}
			final DatabaseMetaData dmd = this.connection.getMetaData();
			final ResultSet rs = dmd.getTables(null, null, table, null);

			return rs.next();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Check database to see if column exists within table.
	 *
	 * @param table Table name to check
	 * @param column Column name to check
	 * @return true if column exists within table, else false
	 */
	public boolean columnExists(final String table, final String column) {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				this.open();
			}
			final DatabaseMetaData dmd = this.connection.getMetaData();
			final ResultSet rs = dmd.getColumns(null, null, table, column);
			return rs.next();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private synchronized void doQuery(final String query) {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				System.out.println("Connection is closed, opening connection...+" + query);
				this.open();
			}
			final PreparedStatement stmt = this.connection.prepareStatement(query);
			stmt.execute();
			stmt.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

}
