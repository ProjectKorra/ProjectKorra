package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class HealingWatersConfig extends AbilityConfig {

	public final long Cooldown = 1500;
	public final long Duration = 1000;
	public final long Interval = 50;
	public final long ChargeTime = 250;
	public final double Range = 5;
	public final int PotionPotency = 2;
	public final boolean EnableParticles = true;
	
	public HealingWatersConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "HealingWaters";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}