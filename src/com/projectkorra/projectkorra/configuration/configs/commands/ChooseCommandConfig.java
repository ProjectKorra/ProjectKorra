package com.projectkorra.projectkorra.configuration.configs.commands;

public class ChooseCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "Error: Player not found!";
	public final String InvalidElement = "Error: Invalid element!";
	public final String OnCooldown = "You must wait %cooldown% before changing your element.";
	public final String SuccessfullyChosen_Other = "{target} is now a {element}.";
	public final String SuccessfullyChosen = "You are now a {element}.";
	public final String SuccessfullyChosenVowel_Other = "{target} is now an {element}.";
	public final String SuccessfullyChosenVowel = "You are now an {element}.";
	
	public ChooseCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Choose";
	}

}
