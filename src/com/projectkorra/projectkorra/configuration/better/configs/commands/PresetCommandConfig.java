package com.projectkorra.projectkorra.configuration.better.configs.commands;

public class PresetCommandConfig extends CommandConfig {

	public final String NoPresets = "";
	public final String NoPresetName = "";
	public final String NoPresetName_External = "";
	public final String Delete = "";
	public final String SuccessfullyBound = "";
	public final String SuccessfullyBound_Other = "";
	public final String FailedToBindAll = "";
	public final String SuccessfullyCopied = "";
	public final String MaxPresets = "";
	public final String AlreadyExists = "";
	public final String Created = "";
	public final String CantEditBinds = "";
	public final String PlayerNotFound = "";
	
	public PresetCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Preset";
	}

}