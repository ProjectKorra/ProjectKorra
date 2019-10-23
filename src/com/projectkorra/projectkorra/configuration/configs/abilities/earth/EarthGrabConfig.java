package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthGrabConfig extends AbilityConfig {

	public final long Cooldown = 4500;
	public final double Range = 30;
	public final double DragSpeed = 1.0;
	public final double DamageThreshold = 4;
	public final double TrapHP = 3;
	public final long TrapHitInterval = 250;
	
	public EarthGrabConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthGrab";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}