package com.projectkorra.projectkorra.storage;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public abstract class Database {
	
	protected final Logger log;
	protected final String prefix;
	protected final String dbprefix;
	protected Connection connection = null;

	public Database(Logger log, String prefix, String dbprefix) {
		this.log = log;
		this.prefix = prefix;
        this.dbprefix = dbprefix;
    }

    /**
     * Print information to console.
     *
     * @param message The string to print to console
     */
    protected void printInfo(String message) {
        log.info(prefix + dbprefix + message);
    }

    /**
     * Print error to console.
     *
     * @param message The string to print to console
     * @param severe If {@param severe} is true print an error, else print a warning
     */
    protected void printErr(String message, boolean severe) {
        if (severe) log.severe(prefix + dbprefix + message);
        else log.warning(prefix + dbprefix + message);
    }

    /**
     * Returns the current Connection.
     *
     * @return Connection if exists, else null
     */
    public Connection getConnection() {
        return connection;
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
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            this.printErr("There was no SQL connection open.", false);
        }
    }

    /**
     * Queries the Database, for queries which modify data. Run async by default.
     *
     * @param query Query to run
     */
    public void modifyQuery(final String query) {
    	modifyQuery(query, true);
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
                    doQuery(query);
                }
            }.runTaskAsynchronously(ProjectKorra.plugin);
        } else {
            doQuery(query);
        }
    }

    /**
     * Queries the Database, for queries which return results.
     *
     * @param query Query to run
     * @return Result set of ran query
     */
    public ResultSet readQuery(String query) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            return rs;
        } catch(SQLException e) {
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
    public boolean tableExists(String table) {
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            ResultSet rs = dmd.getTables(null, null, table, null);
            
            return rs.next();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private synchronized void doQuery(final String query) {
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
