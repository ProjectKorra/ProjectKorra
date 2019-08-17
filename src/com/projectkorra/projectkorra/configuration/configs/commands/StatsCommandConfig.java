package com.projectkorra.projectkorra.configuration.configs.commands;

public class StatsCommandConfig extends CommandConfig {

	public final String InvalidLookup = "";
	public final String InvalidSearchType = "";
	public final String InvalidStatistic = "";
	public final String InvalidPlayer = "";
	
	public StatsCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Stats";
	}

}