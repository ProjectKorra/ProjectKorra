package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TornadoConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final long Duration = 10000;
	public final double Range = 15;
	public final double Height = 15;
	public final double PlayerPushFactor = 1.0;
	public final double NpcPushFactor = 1.0;
	public final double Radius = 10;
	public final double Speed = 1;
	
	public TornadoConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Tornado";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}