package com.projectkorra.projectkorra.configuration.configs.properties;

import com.projectkorra.projectkorra.configuration.Config;

public class CommandPropertiesConfig implements Config {

	public final String NoPermission = "Error: Invalid permissions.";
	public final String MustBePlayer = "Error: Target must be a player.";
	
	public final String BendingPermanentlyRemoved = "Error: Target's bending is permanently removed.";
	public final String BendingPermanentlyRemoved_Other = "";
	
	@Override
	public String getName() {
		return "Command";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}