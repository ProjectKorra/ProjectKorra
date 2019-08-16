package com.projectkorra.projectkorra.configuration.better.configs.commands;

public class CheckCommandConfig extends CommandConfig {

	public final String NewVersionAvailable = "";
	public final String CurrentVersion = "";
	public final String LatestVersion = "";
	public final String UpToDate = "";
	
	public CheckCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Check";
	}

}