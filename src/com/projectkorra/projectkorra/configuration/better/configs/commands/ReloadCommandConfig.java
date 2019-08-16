package com.projectkorra.projectkorra.configuration.better.configs.commands;

public class ReloadCommandConfig extends CommandConfig {

	public final String SuccessfullyReloaded = "";
	
	public ReloadCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Reload";
	}

}