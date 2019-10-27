package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IlluminationConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Range = 3;
	public final byte LightThreshold = 7;
	public final boolean Passive = true;
	
	public IlluminationConfig() {
		super(true, "Illumination is a basic firebending technique that allows firebenders to manipulate their fire to create a light source. This ability will automatically activate when you're in low light.", "Left click to enable. Additionally, left click to disable.");
	}

	@Override
	public String getName() {
		return "Illumination";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}