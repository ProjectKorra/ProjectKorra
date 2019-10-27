package com.projectkorra.projectkorra.configuration.configs.commands;

public class HelpCommandConfig extends CommandConfig {

	public final String Required = "REQUIRED";
	public final String Optional = "OPTIONAL";
	public final String ProperUsage = "Proper Usage: {command1} or {command2}";
	public final String LearnMore = "Learn more on our wiki!";
	public final String InvalidTopic = "That isn't a valid help topic. Use /bending help for more information.";
	public final String Usage = "USAGE:";
	public final String RPGUsage = "";
	public final String SpiritsUsage = "";
	public final String ItemsUsage = "";
	
	public HelpCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Help";
	}

}