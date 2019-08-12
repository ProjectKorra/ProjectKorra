package com.projectkorra.projectkorra.configuration.better.configs.abilities;

import com.projectkorra.projectkorra.configuration.better.Config;

public abstract class AbilityConfig implements Config {
	
	public final boolean Enabled;
	public final String Description;
	public final String Instructions;
	
	public AbilityConfig(boolean enabled, String description, String instructions) {
		Enabled = enabled;
		Description = description;
		Instructions = instructions;
	}
	
}