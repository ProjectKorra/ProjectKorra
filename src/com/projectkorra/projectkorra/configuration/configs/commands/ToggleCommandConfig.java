package com.projectkorra.projectkorra.configuration.configs.commands;

public class ToggleCommandConfig extends CommandConfig {

	public final String ToggledOn = "";
	public final String ToggledOn_ByOther = "";
	public final String ToggledOn_Other = "";
	public final String ToggledOn_All = "";
	public final String ToggledOff = "";
	public final String ToggledOff_ByOther = "";
	public final String ToggledOff_Other = "";
	public final String ToggledOff_All = "";
	public final String ToggledOffForAll = "";
	public final String ToggledOffSingleElement = "";
	public final String ToggledOffSingleElement_ByOther = "";
	public final String ToggledOffSingleElement_Other = "";
	public final String ToggledOnSingleElement = "";
	public final String ToggledOnSingleElement_ByOther = "";
	public final String ToggledOnSingleElement_Other = "";
	public final String WrongElement = "";
	public final String WrongElement_Other = "";
	public final String PlayerNotFound = "";
	
	public ToggleCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Toggle";
	}

}