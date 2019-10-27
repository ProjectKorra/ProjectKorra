package com.projectkorra.projectkorra.configuration.configs.commands;

public class InvincibleCommandConfig extends CommandConfig {

	public final String ToggledOn = "Bending invincibility toggled on";
	public final String ToggledOff = "Bending invincibility toggled off";
	
	public InvincibleCommandConfig() {
		super("");
	}
	
	@Override
	public String getName() {
		return "Invincible";
	}

}