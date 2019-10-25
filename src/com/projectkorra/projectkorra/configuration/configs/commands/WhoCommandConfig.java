package com.projectkorra.projectkorra.configuration.configs.commands;

public class WhoCommandConfig extends CommandConfig {

	public final String DatabaseOverload = "The database appears to be overloaded. Please try again later.";
	public final String NoPlayersOnline = "There is no one online.";
	public final String PlayerOffline = "NOTE: {target} is currently offline. A database lookup is currently being done (this might take a few seconds).";
	
	public WhoCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Who";
	}

}