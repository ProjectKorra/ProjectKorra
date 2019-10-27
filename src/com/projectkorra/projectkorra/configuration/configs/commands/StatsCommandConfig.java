package com.projectkorra.projectkorra.configuration.configs.commands;

public class StatsCommandConfig extends CommandConfig {

	public final String InvalidLookup = "Error: Invalid lookup argument.";
	public final String InvalidSearchType = "Error: Invalid search type.";
	public final String InvalidStatistic = "Error: Invalid statistic.";
	public final String InvalidPlayer = "Error: Invalid player.";
	
	public StatsCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Stats";
	}

}