package com.projectkorra.projectkorra.configuration.configs.commands;

public class WhoCommandConfig extends CommandConfig {

	public final String DatabaseOverload = "";
	public final String NoPlayersOnline = "";
	public final String PlayerOffline = "";
	
	public WhoCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Who";
	}

}