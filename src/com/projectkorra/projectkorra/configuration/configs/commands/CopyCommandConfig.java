package com.projectkorra.projectkorra.configuration.configs.commands;

public class CopyCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "";
	public final String SuccessfullyCopied = "";
	public final String SuccessfullyCopied_Other = "";
	public final String FailedToBindAll = "";
	
	public CopyCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Copy";
	}

}