package com.projectkorra.projectkorra.configuration.configs.properties;

import com.projectkorra.projectkorra.configuration.Config;

public class ChiPropertiesConfig implements Config {

	public final String Description = "";
	
	public final double BlockChiChance = 0;
	public final long BlockChiDuration = 0;
	
	@Override
	public String getName() {
		return "Chi";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}
