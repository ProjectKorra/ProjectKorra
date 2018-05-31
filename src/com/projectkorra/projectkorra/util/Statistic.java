package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;

public enum Statistic {

	PLAYER_KILLS("PlayerKills", "player kills"), PLAYER_DAMAGE("PlayerDamage", "player damage"), TOTAL_KILLS("TotalKills", "total kills"), TOTAL_DAMAGE("TotalDamage", "total damage");

	private String name;
	private String displayName;

	private Statistic(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getStatisticName(CoreAbility ability) {
		return getName() + "_" + ability.getName();
	}

	public static Statistic getStatistic(String name) {
		for (Statistic statistic : Statistic.values()) {
			if (statistic.getName().equalsIgnoreCase(name)) {
				return statistic;
			}
		}
		return null;
	}

}
