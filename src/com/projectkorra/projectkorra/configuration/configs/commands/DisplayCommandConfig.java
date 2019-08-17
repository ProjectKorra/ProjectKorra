package com.projectkorra.projectkorra.configuration.configs.commands;

public class DisplayCommandConfig extends CommandConfig {

	public final String NoCombosAvailable = "";
	public final String NoPassivesAvailable = "";
	public final String NoAbilitiesAvailable = "";
	public final String InvalidArgument = "";
	public final String NoBinds = "";
	
	public DisplayCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Display";
	}

}