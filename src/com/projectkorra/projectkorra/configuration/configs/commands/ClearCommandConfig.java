package com.projectkorra.projectkorra.configuration.configs.commands;

public class ClearCommandConfig extends CommandConfig {

	public final String CantEditBinds = "";
	public final String Cleared = "";
	public final String WrongNumber = "";
	public final String ClearedSlot = "";
	public final String AlreadyEmpty = "";
	
	public ClearCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Clear";
	}

}