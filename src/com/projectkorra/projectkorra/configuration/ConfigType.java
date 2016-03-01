package com.projectkorra.projectkorra.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigType {
	
	public static final ConfigType DEFAULT = new ConfigType("Default");
	public static final ConfigType PRESETS = new ConfigType("Presets");
	public static final ConfigType DEATH_MESSAGE = new ConfigType("DeathMessage");
	public static final ConfigType[] CORE_TYPES = {DEFAULT, PRESETS, DEATH_MESSAGE};
	
	private static HashMap<String, ConfigType> allTypes = new HashMap<>();
	private static List<ConfigType> addonTypes = new ArrayList<>();
	
	private String string;
	
	public ConfigType(String string) {
		this.string = string;
		allTypes.put(string, this);
		if (!Arrays.asList(CORE_TYPES).contains(this)) {
			addonTypes.add(this);
		}
	}
	
	public static List<ConfigType> addonValues() {
		return addonTypes;
	}
	
	public static ConfigType[] coreValues() {
		return CORE_TYPES;
	}
	
	public String toString() {
		return string;
	}
	
	public static List<ConfigType> values() {
		List<ConfigType> values = new ArrayList<>();
		for (String key : allTypes.keySet()) {
			values.add(allTypes.get(key));
		}
		return values;
	}
}
