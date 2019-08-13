package com.projectkorra.projectkorra.configuration.better.configs.commands;

import com.projectkorra.projectkorra.configuration.better.Config;

public class BindCommandConfig implements Config {

	public final String Description = "";
	public final String SuccessfullyBoundMessage = "";
	public final String AbilityDoesntExistMessage = "";
	public final String WrongNumberMessage = "";
	public final String LoadingInfoMessage = "";
	public final String ElementToggledOffMessage = "";
	public final String NoElementMessage = "";
	public final String NoElementMessageAE = "";
	public final String NoSubElementMessage = "";
	public final String UnbindableMessage = "";
	
	@Override
	public String getName() {
		return "Bind";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Commands" };
	}

}