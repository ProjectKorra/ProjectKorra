package com.projectkorra.projectkorra.configuration.configs.commands;

public class ReloadCommandConfig extends CommandConfig {

	public final String SuccessfullyReloaded = "Bending Configuration Reloaded";
	
	public ReloadCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Reload";
	}

}