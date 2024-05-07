package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;

public enum Statistic {

	PLAYER_KILLS("PlayerKills", "player kills"), PLAYER_DAMAGE("PlayerDamage", "player damage"), TOTAL_KILLS("TotalKills", "total kills"), TOTAL_DAMAGE("TotalDamage", "total damage");

	private String name;
	private String displayName;

	private Statistic(final String name, final String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getStatisticName(final CoreAbility ability) {
		return this.getName() + "_" + ability.getName();
	}

	public static Statistic getStatistic(final String name) {
		for (final Statistic statistic : Statistic.values()) {
			if (statistic.getName().equalsIgnoreCase(name)) {
				return statistic;
			}
		}
		return null;
	}

}
