package com.projectkorra.projectkorra.configuration.configs.commands;

public class AddCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "";
	public final String InvalidElement = "";
	public final String SuccessfullyAdded_Other = "";
	public final String SuccessfullyAdded = "";
	public final String SuccessfullyAddedVowel_Other = "";
	public final String SuccessfullyAddedVowel = "";
	public final String SuccessfullyAddedAll_Other = "";
	public final String SuccessfullyAddedAll = "";
	public final String AlreadyHasElement_Other = "";
	public final String AlreadyHasElement = "";
	public final String AlreadyHasSubElement_Other = "";
	public final String AlreadyHasSubElement = "";
	public final String AlreadyHasAllElements_Other = "";
	public final String AlreadyHasAllElements = "";
	
	public AddCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Add";
	}

}