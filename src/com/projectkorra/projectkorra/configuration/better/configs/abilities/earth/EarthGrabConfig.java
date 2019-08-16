package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthGrabConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Range = 0;
	public final double DragSpeed = 0;
	public final double DamageThreshold = 0;
	public final double TrapHP = 0;
	public final long TrapHitInterval = 0;
	
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