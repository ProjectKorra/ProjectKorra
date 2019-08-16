package com.projectkorra.projectkorra.configuration.better.configs.commands;

public class RemoveCommandConfig extends CommandConfig {

	public final String RemovedElement = "";
	public final String RemovedElement_Other = "";
	public final String RemovedElement_ByOther = "";
	public final String RemovedAllElements_Other = "";
	public final String RemovedAllElements_ByOther = "";
	public final String InvalidElement = "";
	public final String WrongElement = "";
	public final String WrongElement_Other = "";
	public final String PlayerOffline = "";
	
	public RemoveCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Remove";
	}

}