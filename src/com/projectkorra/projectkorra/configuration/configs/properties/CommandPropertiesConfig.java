package com.projectkorra.projectkorra.configuration.configs.properties;

import com.projectkorra.projectkorra.configuration.Config;

public class CommandPropertiesConfig implements Config {

	public final String NoPermission = "";
	public final String MustBePlayer = "";
	
	public final String BendingPermanentlyRemoved = "";
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