package com.projectkorra.projectkorra.configuration.configs.commands;

public class CheckCommandConfig extends CommandConfig {

	public final String NewVersionAvailable = "There's a new version of ProjectKorra available!";
	public final String CurrentVersion = "Current Version: {version}";
	public final String LatestVersion = "Latest Version: {version}";
	public final String UpToDate = "You have the latest version of ProjectKorra.";
	
	public CheckCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Check";
	}

}
