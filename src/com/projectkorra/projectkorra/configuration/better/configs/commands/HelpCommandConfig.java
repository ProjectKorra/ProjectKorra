package com.projectkorra.projectkorra.configuration.better.configs.commands;

public class HelpCommandConfig extends CommandConfig {

	public final String Required = "";
	public final String Optional = "";
	public final String ProperUsage = "";
	public final String LearnMore = "";
	public final String InvalidTopic = "";
	public final String Usage = "";
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