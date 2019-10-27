package com.projectkorra.projectkorra.configuration.configs.commands;

public class CopyCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "Error: Player not found!";
	public final String SuccessfullyCopied = "Your binds have been set to match {target}'s!";
	public final String SuccessfullyCopied_Other = "{target1}'s binds have been set to match {target2}'s.";
	public final String FailedToBindAll = "Error: Failed to copy and bind all abilities";
	
	public CopyCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Copy";
	}

}
