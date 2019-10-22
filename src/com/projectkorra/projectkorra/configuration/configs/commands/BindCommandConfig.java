package com.projectkorra.projectkorra.configuration.configs.commands;

public class BindCommandConfig extends CommandConfig {

	public final String SuccessfullyBoundMessage = "Succesfully bound {ability} to slot {slot}.";
	public final String AbilityDoesntExistMessage = "{ability} is not a valid ability.";
	public final String WrongNumberMessage = "Slot must be an integer between 1 and 9.";
	public final String LoadingInfoMessage = "";
	public final String ElementToggledOffMessage = "You have that ability's element toggled off currently.";
	public final String NoElementMessage = "You are not a {element}!";
	public final String NoElementMessageVowel = "You are not an {element}!";
	public final String NoSubElementMessage = "You don't have access to {subelement}!";
	public final String NoSubElementMessageVowel = "You don't have access to {subelement}!";
	public final String UnbindableMessage = "{ability} cannot be bound!";
	
	public BindCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Bind";
	}

}
