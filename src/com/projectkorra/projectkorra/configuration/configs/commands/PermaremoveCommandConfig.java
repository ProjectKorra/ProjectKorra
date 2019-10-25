package com.projectkorra.projectkorra.configuration.configs.commands;

public class PermaremoveCommandConfig extends CommandConfig {

	public final String PlayerOffline = "Error: Player is offline!";
	public final String Restored = "Your bending has been restored";
	public final String Restored_Other = "You have restored the bending of {target}.";
	public final String Removed = "Your bending has been permanently removed.";
	public final String Removed_Other = "You have removed the bending of {target}.";
	
	public PermaremoveCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Permaremove";
	}

}