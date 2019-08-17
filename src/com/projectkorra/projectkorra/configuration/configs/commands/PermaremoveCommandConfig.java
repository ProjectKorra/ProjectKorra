package com.projectkorra.projectkorra.configuration.configs.commands;

public class PermaremoveCommandConfig extends CommandConfig {

	public final String PlayerOffline = "";
	public final String Restored = "";
	public final String Restored_Other = "";
	public final String Removed = "";
	public final String Removed_Other = "";
	
	public PermaremoveCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Permaremove";
	}

}