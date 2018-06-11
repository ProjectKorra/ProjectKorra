package com.projectkorra.projectkorra.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.storage.DBConnection;

public class StatisticsMethods {

	/**
	 * Get the {@link Statistic} value of the given {@link CoreAbility} for the
	 * {@link Player} with {@link UUID} uuid.
	 * 
	 * @param uuid The {@link UUID} of the {@link Player} being looked up.
	 * @param ability The {@link CoreAbility} for which the given statistic is
	 *            being looked up against.
	 * @param statistic The {@link Statistic} being searched under.
	 * 
	 * @return The found value of the given statistic. If the target player is
	 *         not online this value will be pulled from the database.
	 *         Otherwise, their current value will be pulled from
	 *         {@link StatisticsManager#STATISTICS}.
	 */
	public static long getStatisticAbility(UUID uuid, CoreAbility ability, Statistic statistic) {
		int statId = getId(statistic.getStatisticName(ability));
		return ProjectKorra.statistics.getStatisticCurrent(uuid, statId);
	}

	/**
	 * Increment the {@link Player} with {@link UUID} uuid's {@link Statistic}
	 * for the given {@link CoreAbility} by a constant.
	 * 
	 * @param uuid The {@link UUID} of the {@link Player} with the
	 *            {@link Statistic} being modified.
	 * @param ability The {@link CoreAbility} for which the given statistic is
	 *            being added to.
	 * @param statistic The {@link Statistic} being modified.
	 * @param statDelta The difference which is to be added onto the user's
	 *            current statistic value.
	 */
	public static void addStatisticAbility(UUID uuid, CoreAbility ability, Statistic statistic, long statDelta) {
		int statId = getId(statistic.getStatisticName(ability));
		ProjectKorra.statistics.addStatistic(uuid, statId, statDelta);
	}

	/**
	 * Get the {@link Statistic} value of the given {@link Element} for the
	 * {@link Player} with {@link UUID} uuid.
	 * 
	 * @param uuid The {@link UUID} of the {@link Player} being looked up.
	 * @param element The {@link Element} for which the given statistic is being
	 *            looked up against.
	 * @param statistic The {@link Statistic} being searched under.
	 * @return The found value of all statistics under this element. If the
	 *         target player is not online this value will be pulled from the
	 *         database. Otherwise, their current value will be pulled from
	 *         {@link StatisticsManager#STATISTICS}.
	 */
	public static long getStatisticElement(UUID uuid, Element element, Statistic statistic) {
		long totalValue = 0;
		for (int statId : ProjectKorra.statistics.getStatisticsMap(uuid).keySet()) {
			String abilName = getAbilityName(statId);
			CoreAbility ability = CoreAbility.getAbility(abilName);
			if (ability == null) {
				continue;
			} else if (!ability.getElement().equals(element)) {
				continue;
			}
			// If the ID for this statistic and ability do not equal statId, then it must be a different statistic type
			else if (getId(statistic.getStatisticName(ability)) != statId) {
				continue;
			}
			long value = getStatisticAbility(uuid, ability, statistic);
			totalValue += value;
		}
		return totalValue;
	}

	/**
	 * Get the {@link Statistic} value of the given {@link Element} for the
	 * {@link Player} with {@link UUID} uuid.
	 * 
	 * @param uuid The {@link UUID} of the {@link Player} being looked up.
	 * @param statistic The {@link Statistic} being searched under.
	 * @return The found value of all statistics under this category. If the
	 *         target player is not online this value will be pulled from the
	 *         database. Otherwise, their current value will be pulled from
	 *         {@link StatisticsManager#STATISTICS}.
	 */
	public static long getStatisticTotal(UUID uuid, Statistic statistic) {
		long totalValue = 0;
		for (int statId : ProjectKorra.statistics.getStatisticsMap(uuid).keySet()) {
			String abilName = getAbilityName(statId);
			CoreAbility ability = CoreAbility.getAbility(abilName);
			if (ability == null) {
				continue;
			}
			// If the ID for this statistic and ability do not equal statId, then it must be a different statistic type
			else if (getId(statistic.getStatisticName(ability)) != statId) {
				continue;
			}
			long value = getStatisticAbility(uuid, ability, statistic);
			totalValue += value;
		}
		return totalValue;
	}

	/**
	 * Get the {@link Statistic} value of the given {@link Object} for the
	 * {@link Player} with {@link UUID} uuid. This method will interpret as to
	 * whether the developer is trying to pull a statistic lookup on an ability
	 * or element.
	 * 
	 * @param uuid The {@link UUID} of the {@link Player} being looked up.
	 * @param object This {@link Object} is used to input either a
	 *            {@link CoreAbility} or {@link Element} when using statistics
	 *            in a more general way.
	 * @param statistic The {@link Statistic} being searched under.
	 * 
	 * @return The found value of the given statistic. If the target player is
	 *         not online this value will be pulled from the database.
	 *         Otherwise, their current value will be pulled from
	 *         {@link StatisticsManager#STATISTICS}.
	 * 
	 * @throws IllegalArgumentException if the given object argument is not of
	 *             type {@link CoreAbility} or {@link Element}.
	 */
	public static long getStatistic(UUID uuid, Object object, Statistic statistic) throws IllegalArgumentException {
		if (object instanceof CoreAbility) {
			return getStatisticAbility(uuid, (CoreAbility) object, statistic);
		} else if (object instanceof Element) {
			return getStatisticElement(uuid, (Element) object, statistic);
		} else {
			throw new IllegalArgumentException("Variable object is not a valid input type. Required: CoreAbility or Element.");
		}
	}

	/**
	 * Get the statistic ID generated by the pk_statKeys table for the statName
	 * {@link String}.
	 * 
	 * @param statName The {@link String} identified used in the pk_statKeys
	 *            table.
	 * @return The ID associated with the provided key. If invalid statName,
	 *         return -1.
	 */
	public static int getId(String statName) {
		if (!ProjectKorra.statistics.getKeysByName().containsKey(statName)) {
			DBConnection.sql.modifyQuery("INSERT INTO pk_statKeys (statName) VALUES ('" + statName + "')", false);
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_statKeys WHERE statName = '" + statName + "'")) {
				if (rs.next()) {
					ProjectKorra.statistics.getKeysByName().put(rs.getString("statName"), rs.getInt("id"));
					ProjectKorra.statistics.getKeysById().put(rs.getInt("id"), rs.getString("statName"));
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ProjectKorra.statistics.getKeysByName().containsKey(statName) ? ProjectKorra.statistics.getKeysByName().get(statName) : -1;
	}

	/**
	 * Get the unique {@link String} used by the pk_statKeys to register new
	 * {@link Statistic} options.
	 * 
	 * @param id The statistic ID associated with this statistic name.
	 * @return The unique statistic name. If invalid id, return an empty
	 *         {@link String}.
	 */
	public static String getAbilityName(int id) {
		if (!ProjectKorra.statistics.getKeysById().containsKey(id)) {
			return "";
		}
		String statName = ProjectKorra.statistics.getKeysById().get(id);
		String[] split = statName.split("_");
		if (split.length < 2) {
			return "";
		}
		return split[1];
	}

}