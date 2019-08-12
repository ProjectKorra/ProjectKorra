package com.projectkorra.projectkorra.configuration.better.configs.abilities;

public abstract class SourcedWaterAbilityConfig extends AbilityConfig {
	
	public final boolean CanAutoSource;
	public final boolean CanDynamicSource;
	
	public SourcedWaterAbilityConfig(boolean enabled, String description, String instructions, boolean canAutoSource, boolean canDynamicSource) {
		super(enabled, description, instructions);
		
		CanAutoSource = canAutoSource;
		CanDynamicSource = canDynamicSource;
	}
	
}