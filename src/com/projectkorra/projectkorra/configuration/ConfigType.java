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
	public static final ConfigType[] CORE_TYPES = { DEFAULT, PRESETS, LANGUAGE };

	private final String string;

	public ConfigType(final String string) {
		this.string = string;
		ALL_TYPES.put(string, this);
	}

	public static List<ConfigType> addonValues() {
		final List<ConfigType> values = new ArrayList<>();
		for (final String key : ALL_TYPES.keySet()) {
			if (!Arrays.asList(CORE_TYPES).contains(ALL_TYPES.get(key))) {
				values.add(ALL_TYPES.get(key));
			}
		}
		return values;
	}

	public static List<ConfigType> coreValues() {
		return Arrays.asList(CORE_TYPES);
	}

	@Override
	public String toString() {
		return this.string;
	}

	public static List<ConfigType> values() {
		final List<ConfigType> values = new ArrayList<>();
		for (final String key : ALL_TYPES.keySet()) {
			values.add(ALL_TYPES.get(key));
		}
		return values;
	}
}
