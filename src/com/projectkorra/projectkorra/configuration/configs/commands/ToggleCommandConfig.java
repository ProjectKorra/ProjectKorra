package com.projectkorra.projectkorra.configuration.configs.commands;

public class ToggleCommandConfig extends CommandConfig {

	public final String ToggledOn = "You have turned your bending on.";
	public final String ToggledOn_ByOther = "Your {element} has been toggled on by {sender}.";
	public final String ToggledOn_Other = "You've toggled on {target}'s {element}";
	public final String ToggledOn_All = "Bending has been toggled on for all players.";
	public final String ToggledOff = "You have turned your bending off.";
	public final String ToggledOff_ByOther = "Your bending has been toggled off. You will not be able to use most abilities until you toggle it back.";
	public final String ToggledOff_Other = "You've toggled off {target}'s {element}.";
	public final String ToggledOff_All = "Bending has been toggled off for all players.";
	public final String ToggledOffForAll = "Bending is currently toggled off for all players.";
	public final String ToggledOffSingleElement = "You have toggled off your {element}.";
	public final String ToggledOffSingleElement_ByOther = "Your {element} has been toggled off by {sender}.";
	public final String ToggledOffSingleElement_Other = "You've toggled off {target}'s {element}";
	public final String ToggledOnSingleElement = "You have toggled on your {element}.";
	public final String ToggledOnSingleElement_ByOther = "Your {element} has been toggled on by {sender}.";
	public final String ToggledOnSingleElement_Other = "You've toggled on {target}'s {element}";
	public final String WrongElement = "Error: You do not have that element";
	public final String WrongElement_Other = "Error: Target does not have that element";
	public final String PlayerNotFound = "Error: Player not found";
	
	public ToggleCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Toggle";
	}

}