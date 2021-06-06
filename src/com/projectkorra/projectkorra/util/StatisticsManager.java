package com.projectkorra.projectkorra.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.storage.MySQL;
import com.projectkorra.projectkorra.storage.SQLite;

public class StatisticsManager extends Manager implements Runnable {

	/**
	 * HashMap which contains all current statistic values (Map<player,
	 * Map<statId, statValue>>)
	 */
	private final Map<UUID, Map<Integer, Long>> STATISTICS = new HashMap<>();
	/**
	 * HashMap which contains all statistic delta values (Map<player,
	 * Map<statId, statValue>>)
	 */
	private final Map<UUID, Map<Integer, Long>> DELTA = new HashMap<>();
	/**
	 * HashMap which contains all statistic names by ID.
	 */
	private final Map<String, Integer> KEYS_BY_NAME = new HashMap<>();
	/**
	 * HashMap which contains all statistic IDs by name.
	 */
	private final Map<Integer, String> KEYS_BY_ID = new HashMap<>();
	/**
	 * HashMap which contains all UUIDs of players who have recently logged out
	 * to have their stats saved.
	 */
	private final Set<UUID> STORAGE = new HashSet<>();
	private final int INTERVAL = 5;

	private StatisticsManager() {}

	@Override
	public void onActivate() {
		if (!ProjectKorra.isStatisticsEnabled()) {
			ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, this, 20 * this.INTERVAL, 20 * this.INTERVAL);
		}
		this.setupStatistics();
	}

	public void setupStatistics() {
		// Create pk_statKeys table.
		if (!DBConnection.sql.tableExists("pk_statKeys")) {
			ProjectKorra.log.info("Creating pk_statKeys table");
			String query = "";
			if (DBConnection.sql instanceof MySQL) {
				query = "CREATE TABLE `pk_statKeys` (`id` INTEGER PRIMARY KEY AUTO_INCREMENT, `statName` VARCHAR(64));";
			} else if (DBConnection.sql instanceof SQLite) {
				query = "CREATE TABLE `pk_statKeys` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `statName` TEXT(64));";
			}
			DBConnection.sql.modifyQuery(query, false);
		}
		// Create pk_stats table.
		if (!DBConnection.sql.tableExists("pk_stats")) {
			ProjectKorra.log.info("Creating pk_stats table");
			String query = "";
			if (DBConnection.sql instanceof MySQL) {
				query = "CREATE TABLE `pk_stats` (`statId` INTEGER, `uuid` VARCHAR(36), `statValue` BIGINT, PRIMARY KEY (statId, uuid));";
			} else if (DBConnection.sql instanceof SQLite) {
				query = "CREATE TABLE `pk_stats` (`statId` INTEGER, `uuid` TEXT(36), `statValue` BIGINT, PRIMARY KEY (statId, uuid));";
			}
			DBConnection.sql.modifyQuery(query, false);
		}
		// Insert all abilities into pk_statKeys for all statistics.
		for (final CoreAbility ability : CoreAbility.getAbilitiesByName()) {
			if (ability.isHarmlessAbility()) {
				continue;
			}
			for (final Statistic statistic : Statistic.values()) {
				final String statName = statistic.getStatisticName(ability);
				final ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_statKeys WHERE statName = '" + statName + "'");
				try {
					if (!rs.next()) {
						DBConnection.sql.modifyQuery("INSERT INTO pk_statKeys (statName) VALUES ('" + statName + "')", false);
					}
					Statement stmt = rs.getStatement();
					rs.close();
					stmt.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		}
		// Populate Keys Map with all loaded statName(s) in pk_statKeys.
		final ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_statKeys");
		try {
			while (rs.next()) {
				this.KEYS_BY_NAME.put(rs.getString("statName"), rs.getInt("id"));
				this.KEYS_BY_ID.put(rs.getInt("id"), rs.getString("statName"));
			}
			Statement stmt = rs.getStatement();
			rs.close();
			stmt.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public void load(final UUID uuid) {
		this.STATISTICS.put(uuid, new HashMap<>());
		this.DELTA.put(uuid, new HashMap<>());
		Statement stmt = null;
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "'")) {
			while (rs.next()) {
				this.STATISTICS.get(uuid).put(rs.getInt("statId"), rs.getLong("statValue"));
				this.DELTA.get(uuid).put(rs.getInt("statId"), 0L);
			}
			stmt = rs.getStatement();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void save(final UUID uuid, final boolean async) {
		if (!this.DELTA.containsKey(uuid)) {
			return;
		}
		final Map<Integer, Long> stats = this.DELTA.get(uuid);
		for (final Entry<Integer, Long> entry : stats.entrySet()) {
			final int statId = entry.getKey();
			final long statValue = entry.getValue();
			Statement stmt = null;
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId)) {
				if (!rs.next()) {
					DBConnection.sql.modifyQuery("INSERT INTO pk_stats (statId, uuid, statValue) VALUES (" + statId + ", '" + uuid.toString() + "', " + statValue + ")", async);
				} else {
					DBConnection.sql.modifyQuery("UPDATE pk_stats SET statValue = statValue + " + statValue + " WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId + ";", async);
				}
				stmt = rs.getStatement();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public long getStatisticDelta(final UUID uuid, final int statId) {
		// If the player is offline, pull value from database.
		if (!this.DELTA.containsKey(uuid)) {
			return 0;
		} else if (!this.DELTA.get(uuid).containsKey(statId)) {
			return 0;
		}
		return this.DELTA.get(uuid).get(statId);
	}

	public long getStatisticCurrent(final UUID uuid, final int statId) {
		// If the player is offline, pull value from database.
		if (!this.STATISTICS.containsKey(uuid)) {
			Statement stmt = null;
			long count = 0;
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT statValue FROM pk_stats WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId + ";")) {
				if (rs.next()) {
					count = rs.getLong("statValue");
				}
				stmt = rs.getStatement();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			return count;
		} else if (!this.STATISTICS.get(uuid).containsKey(statId)) {
			return 0;
		}
		return this.STATISTICS.get(uuid).get(statId);
	}

	public void addStatistic(final UUID uuid, final int statId, final long statDelta) {
		if (!this.STATISTICS.containsKey(uuid) || !this.DELTA.containsKey(uuid)) {
			return;
		}
		this.STATISTICS.get(uuid).put(statId, this.getStatisticCurrent(uuid, statId) + statDelta);
		this.DELTA.get(uuid).put(statId, this.getStatisticDelta(uuid, statId) + statDelta);

	}

	public Map<Integer, Long> getStatisticsMap(final UUID uuid) {
		final Map<Integer, Long> map = new HashMap<>();
		// If the player is offline, create a new temporary Map from the database.
		if (!this.STATISTICS.containsKey(uuid)) {
			Statement stmt = null;
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "'")) {
				while (rs.next()) {
					final int statId = rs.getInt("statId");
					final long statValue = rs.getLong("statValue");
					map.put(statId, statValue);
				}
				stmt = rs.getStatement();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
			return map;
		}
		return this.STATISTICS.get(uuid);
	}

	public void store(final UUID uuid) {
		this.STORAGE.add(uuid);
	}

	@Override
	public void run() {
		for (final UUID uuid : this.STORAGE) {
			// Confirm that the player is offline.
			final Player player = ProjectKorra.plugin.getServer().getPlayer(uuid);
			if (player == null) {
				this.save(uuid, true);
			}
		}
		this.STORAGE.clear();
	}

	public Map<String, Integer> getKeysByName() {
		return this.KEYS_BY_NAME;
	}

	public Map<Integer, String> getKeysById() {
		return this.KEYS_BY_ID;
	}

}
