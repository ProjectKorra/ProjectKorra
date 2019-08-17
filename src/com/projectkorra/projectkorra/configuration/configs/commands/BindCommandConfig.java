package com.projectkorra.projectkorra.configuration.configs.commands;

public class BindCommandConfig extends CommandConfig {

	public final String SuccessfullyBoundMessage = "";
	public final String AbilityDoesntExistMessage = "";
	public final String WrongNumberMessage = "";
	public final String LoadingInfoMessage = "";
	public final String ElementToggledOffMessage = "";
	public final String NoElementMessage = "";
	public final String NoElementMessageAE = "";
	public final String NoSubElementMessage = "";
	public final String UnbindableMessage = "";
	
	public BindCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Bind";
	}

}