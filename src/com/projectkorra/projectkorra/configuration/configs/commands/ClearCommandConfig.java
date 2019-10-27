package com.projectkorra.projectkorra.configuration.configs.commands;

public class ClearCommandConfig extends CommandConfig {

	public final String CantEditBinds = "Error: You can't edit your binds right now!";
	public final String Cleared = "Your bound abilities have been cleared";
	public final String WrongNumber = "Error: Slot number must be an integer between 1-9";
	public final String ClearedSlot = "You have cleared slot #{slot}.";
	public final String AlreadyEmpty = "That slot is already empty.";
	
	public ClearCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Clear";
	}

}
