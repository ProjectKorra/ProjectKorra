package com.projectkorra.projectkorra.configuration.configs.properties;

import com.projectkorra.projectkorra.configuration.Config;

public class ChiPropertiesConfig implements Config {

	public final String Description = "";
	
	public final double BlockChiChance = 25;
	public final long BlockChiDuration = 1000;
	
	@Override
	public String getName() {
		return "Chi";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}
