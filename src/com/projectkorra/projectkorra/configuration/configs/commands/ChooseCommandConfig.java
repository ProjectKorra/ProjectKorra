package com.projectkorra.projectkorra.configuration.configs.commands;

public class ChooseCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "";
	public final String InvalidElement = "";
	public final String OnCooldown = "";
	public final String SuccessfullyChosen_Other = "";
	public final String SuccessfullyChosen = "";
	public final String SuccessfullyChosenVowel_Other = "";
	public final String SuccessfullyChosenVowel = "";
	
	public ChooseCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Choose";
	}

}