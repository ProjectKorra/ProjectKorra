package com.projectkorra.projectkorra.configuration.configs.commands;

public class AddCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "Error: Player not found!";
	public final String InvalidElement = "Error: Invalid element!";
	public final String SuccessfullyAdded_Other = "{target} is now also a {element}.";
	public final String SuccessfullyAdded = "You are now also a {element}.";
	public final String SuccessfullyAddedVowel_Other = "{target} is now also an {element}.";
	public final String SuccessfullyAddedVowel = "You are now also an {element}.";
	public final String SuccessfullyAddedAll_Other = "{target} now also has: ";
	public final String SuccessfullyAddedAll = "You now also have: ";
	public final String AlreadyHasElement_Other = "Error: Target already has element!";
	public final String AlreadyHasElement = "Error: You already have that element!";
	public final String AlreadyHasSubElement_Other = "Error: Target already has that subelement!";
	public final String AlreadyHasSubElement = "Error: You already have that subelement!";
	public final String AlreadyHasAllElements_Other = "Error: Already has all elements!";
	public final String AlreadyHasAllElements = "Error: Already has all elements!";
	
	public AddCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Add";
	}

}
