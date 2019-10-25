package com.projectkorra.projectkorra.configuration.configs.commands;

public class DisplayCommandConfig extends CommandConfig {

	public final String NoCombosAvailable = "There are no {element} combos available.";
	public final String NoPassivesAvailable = "There are no {element} passives available.";
	public final String NoAbilitiesAvailable = "There are no {element} abilities available.";
	public final String InvalidArgument = "Error: Invalid argument!";
	public final String NoBinds = "You do not have any abilities bound.\\nIf you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.";
	
	public DisplayCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Display";
	}

}