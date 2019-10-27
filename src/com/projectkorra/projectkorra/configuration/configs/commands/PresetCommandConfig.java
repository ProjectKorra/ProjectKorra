package com.projectkorra.projectkorra.configuration.configs.commands;

public class PresetCommandConfig extends CommandConfig {

	public final String NoPresets = "Error: You do not have any presets.";
	public final String NoPresetName = "Error: You do not have any presets with that name.";
	public final String NoPresetName_External = "Error: No external preset found with that name.";
	public final String Delete = "You have deleted your '{name}' preset.";
	public final String SuccessfullyBound = "Your binds have been set to match the {name} preset.";
	public final String SuccessfullyBound_Other = "The bound slots of {target} have been set to match the {name} preset.";
	public final String FailedToBindAll = "Error: Some abilities were not bound due to missing elements!";
	public final String SuccessfullyCopied = "Your binds have been set to match {target}'s binds.";
	public final String MaxPresets = "Error: You have reached your maximum amount of presets!";
	public final String AlreadyExists = "Error: Preset with that name already exists!";
	public final String Created = "Created a new preset named '{name}'.";
	public final String CantEditBinds = "Error: Can't edit binds!";
	public final String PlayerNotFound = "Error: Player not found!";
	
	public PresetCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Preset";
	}

}