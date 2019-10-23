package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirScooterConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final double Speed = .7;
	public final double Interval = 100;
	public final double Radius = 1;
	public final long Duration = 0;
	public final double MaxHeightFromGround = 7;
	public final boolean ShowSitting = true;
	
	public AirScooterConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirScooter";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}