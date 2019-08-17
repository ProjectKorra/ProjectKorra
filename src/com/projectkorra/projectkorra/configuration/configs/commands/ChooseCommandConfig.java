package com.projectkorra.projectkorra.configuration.configs.commands;

public class ChooseCommandConfig extends CommandConfig {

	public final String PlayerNotFound = "";
	public final String InvalidElement = "";
	public final String OnCooldown = "";
	public final String SuccessfullyChosenCFW_Other = "";
	public final String SuccessfullyChosenCFW = "";
	public final String SuccessfullyChosenAE_Other = "";
	public final String SuccessfullyChosenAE = "";
	
	public ChooseCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Choose";
	}

}