package com.projectkorra.projectkorra.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.storage.MySQL;
import com.projectkorra.projectkorra.storage.SQLite;

public class StatisticsManager implements Runnable {

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

	public StatisticsManager() {
		if (!ProjectKorra.isStatisticsEnabled()) {
			ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, this, 20 * INTERVAL, 20 * INTERVAL);
		}
		setupStatistics();
	}

	public void setupStatistics() {
		// Create pk_statKeys table
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
		// Create pk_stats table
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
		// Insert all abilities into pk_statKeys for all statistics
		for (CoreAbility ability : CoreAbility.getAbilitiesByName()) {
			if (ability.isHarmlessAbility()) {
				continue;
			}
			for (Statistic statistic : Statistic.values()) {
				String statName = statistic.getStatisticName(ability);
				ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_statKeys WHERE statName = '" + statName + "'");
				try {
					if (!rs.next()) {
						DBConnection.sql.modifyQuery("INSERT INTO pk_statKeys (statName) VALUES ('" + statName + "')", false);
					}
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		// Populate Keys Map with all loaded statName(s) in pk_statKeys
		ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_statKeys");
		try {
			while (rs.next()) {
				KEYS_BY_NAME.put(rs.getString("statName"), rs.getInt("id"));
				KEYS_BY_ID.put(rs.getInt("id"), rs.getString("statName"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void load(UUID uuid) {
		STATISTICS.put(uuid, new HashMap<>());
		DELTA.put(uuid, new HashMap<>());
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "'")) {
			while (rs.next()) {
				STATISTICS.get(uuid).put(rs.getInt("statId"), rs.getLong("statValue"));
				DELTA.get(uuid).put(rs.getInt("statId"), 0L);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void save(UUID uuid, boolean async) {
		if (!DELTA.containsKey(uuid)) {
			return;
		}
		Map<Integer, Long> stats = DELTA.get(uuid);
		for (Entry<Integer, Long> entry : stats.entrySet()) {
			int statId = entry.getKey();
			long statValue = entry.getValue();
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId)) {
				if (!rs.next()) {
					DBConnection.sql.modifyQuery("INSERT INTO pk_stats (statId, uuid, statValue) VALUES (" + statId + ", '" + uuid.toString() + "', " + statValue + ")", async);
				} else {
					DBConnection.sql.modifyQuery("UPDATE pk_stats SET statValue = statValue + " + statValue + " WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId + ";", async);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public long getStatisticDelta(UUID uuid, int statId) {
		// If the player is offline, pull value from database
		if (!DELTA.containsKey(uuid)) {
			return 0;
		} else if (!DELTA.get(uuid).containsKey(statId)) {
			return 0;
		}
		return DELTA.get(uuid).get(statId);
	}

	public long getStatisticCurrent(UUID uuid, int statId) {
		// If the player is offline, pull value from database
		if (!STATISTICS.containsKey(uuid)) {
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT statValue FROM pk_stats WHERE uuid = '" + uuid.toString() + "' AND statId = " + statId + ";")) {
				if (rs.next()) {
					return rs.getLong("statValue");
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		} else if (!STATISTICS.get(uuid).containsKey(statId)) {
			return 0;
		}
		return STATISTICS.get(uuid).get(statId);
	}

	public void addStatistic(UUID uuid, int statId, long statDelta) {
		if (!STATISTICS.containsKey(uuid) || !DELTA.containsKey(uuid)) {
			return;
		}
		STATISTICS.get(uuid).put(statId, getStatisticCurrent(uuid, statId) + statDelta);
		DELTA.get(uuid).put(statId, getStatisticDelta(uuid, statId) + statDelta);

	}

	public Map<Integer, Long> getStatisticsMap(UUID uuid) {
		Map<Integer, Long> map = new HashMap<>();
		// If the player is offline, create a new temporary Map from the database
		if (!STATISTICS.containsKey(uuid)) {
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_stats WHERE uuid = '" + uuid.toString() + "'")) {
				while (rs.next()) {
					int statId = rs.getInt("statId");
					long statValue = rs.getLong("statValue");
					map.put(statId, statValue);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			return map;
		}
		return STATISTICS.get(uuid);
	}

	public void store(UUID uuid) {
		STORAGE.add(uuid);
	}

	@Override
	public void run() {
		for (UUID uuid : STORAGE) {
			// Confirm that the player is offline
			Player player = ProjectKorra.plugin.getServer().getPlayer(uuid);
			if (player == null) {
				save(uuid, true);
			}
		}
		STORAGE.clear();
	}

	public Map<String, Integer> getKeysByName() {
		return KEYS_BY_NAME;
	}

	public Map<Integer, String> getKeysById() {
		return KEYS_BY_ID;
	}

}