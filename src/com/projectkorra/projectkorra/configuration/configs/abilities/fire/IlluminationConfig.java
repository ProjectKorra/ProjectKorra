package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IlluminationConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Range = 0;
	public final byte LightThreshold = 0;
	public final boolean Passive = true;
	
	public IlluminationConfig() {
		super(true, "", "");
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