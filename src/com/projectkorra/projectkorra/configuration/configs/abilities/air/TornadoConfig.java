package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TornadoConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Range = 0;
	public final double Height = 0;
	public final double PlayerPushFactor = 0;
	public final double NpcPushFactor = 0;
	public final double Radius = 0;
	public final double Speed = 0;
	
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