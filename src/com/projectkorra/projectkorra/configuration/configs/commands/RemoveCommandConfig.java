package com.projectkorra.projectkorra.configuration.configs.commands;

public class RemoveCommandConfig extends CommandConfig {

	public final String RemovedElement = "You've removed your {element}.";
	public final String RemovedElement_Other = "You removed {target}'s {element}.";
	public final String RemovedElement_ByOther = "Your {element} has been removed by {sender}.";
	public final String RemovedAllElements_Other = "You've removed {target}'s bending.";
	public final String RemovedAllElements_ByOther = "Your bending has been removed by {sender}.";
	public final String InvalidElement = "Error: Invalid element!";
	public final String WrongElement = "Error: You do not have that element!";
	public final String WrongElement_Other = "Error: {target} does not have that element!";
	public final String PlayerOffline = "Error: Player not online!";
	
	public RemoveCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Remove";
	}

}