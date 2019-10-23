package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TremorsenseConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final byte LightThreshold = 0;
	public final int MaxDepth = 0;
	public final int StickyRange = 0;
	public final int Radius = 0;
	
	public TremorsenseConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Tremorsense";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}