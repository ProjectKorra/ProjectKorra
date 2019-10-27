package com.projectkorra.projectkorra.configuration.configs.commands;

public class DebugCommandConfig extends CommandConfig {

	public final String SuccessfullyExported = "Debug File Created as debug.txt in the ProjectKorra plugin folder.\\nPut contents on pastie.org and create a bug report  on the ProjectKorra forum if you need to.'";
	
	public DebugCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Debug";
	}

}