package com.projectkorra.projectkorra.configuration.configs.properties;

import com.projectkorra.projectkorra.configuration.Config;

public class AvatarPropertiesConfig implements Config {

	public final String Description = "";
	
	@Override
	public String getName() {
		return "Avatar";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}
