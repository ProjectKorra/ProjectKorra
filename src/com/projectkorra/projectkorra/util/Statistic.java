package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;

public enum Statistic {

	KILLS, DAMAGE;

	public String getStatisticName(CoreAbility ability) {
		return name() + "_" + ability.getName();
	}

	public static Statistic getStatistic(String name) {
		for (Statistic statistic : Statistic.values()) {
			if (statistic.name().equalsIgnoreCase(name)) {
				return statistic;
			}
		}
		return null;
	}

}
