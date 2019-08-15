package com.projectkorra.projectkorra.configuration.better.configs.properties;

import com.projectkorra.projectkorra.configuration.better.Config;

public class ChiPropertiesConfig implements Config {

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
