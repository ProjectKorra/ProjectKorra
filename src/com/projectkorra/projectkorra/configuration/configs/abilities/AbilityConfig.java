package com.projectkorra.projectkorra.configuration.configs.abilities;

import com.projectkorra.projectkorra.configuration.Config;

public abstract class AbilityConfig implements Config {
	
	public final boolean Enabled;
	public final String Description;
	public final String Instructions;
	
	public final String DeathMessage;
	public final String HorizontalVelocityDeathMessage;
	
	public AbilityConfig(boolean enabled, String description, String instructions, String deathMessage, String horizontalVelocityDeathMessage) {
		Enabled = enabled;
		Description = description;
		Instructions = instructions;
		DeathMessage = deathMessage;
		HorizontalVelocityDeathMessage = horizontalVelocityDeathMessage;
	}
	
	public AbilityConfig(boolean enabled, String description, String instructions) {
		this(enabled, description, instructions, null, null);
	}
	
}