package com.projectkorra.projectkorra.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigType {
	
	private static final HashMap<String, ConfigType> ALL_TYPES = new HashMap<>();
	
	public static final ConfigType DEFAULT = new ConfigType("Default");
	public static final ConfigType PRESETS = new ConfigType("Presets");
	public static final ConfigType LANGUAGE = new ConfigType("Language");
	public static final ConfigType AVATAR = new ConfigType("Avatar");
	public static final ConfigType WATER = new ConfigType("Water");
	public static final ConfigType EARTH = new ConfigType("Earth");
	public static final ConfigType FIRE = new ConfigType("Fire");
	public static final ConfigType AIR = new ConfigType("Air");
	public static final ConfigType CHI = new ConfigType("Chi");
	public static final ConfigType ADDONS = new ConfigType("AddonAbilities");
	public static final ConfigType[] CORE_TYPES = {DEFAULT, PRESETS, LANGUAGE, AVATAR, WATER, EARTH, FIRE, AIR, CHI, ADDONS};
	
	private String string;
	
	public ConfigType(String string) {
		this.string = string;
		ALL_TYPES.put(string, this);
	}
	
	public static List<ConfigType> addonValues() {
		List<ConfigType> values = new ArrayList<>();
		for (String key : ALL_TYPES.keySet()) {
			if (!Arrays.asList(CORE_TYPES).contains(ALL_TYPES.get(key))) {
				values.add(ALL_TYPES.get(key));
			}
		}
		return values;
	}
	
	public static List<ConfigType> coreValues() {
		return Arrays.asList(CORE_TYPES);
	}
	
	public String toString() {
		return string;
	}
	
	public static List<ConfigType> values() {
		List<ConfigType> values = new ArrayList<>();
		for (String key : ALL_TYPES.keySet()) {
			values.add(ALL_TYPES.get(key));
		}
		return values;
	}
}
