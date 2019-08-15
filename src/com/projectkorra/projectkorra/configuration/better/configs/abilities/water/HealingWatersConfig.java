package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class HealingWatersConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final long Interval = 0;
	public final long ChargeTime = 0;
	public final double Range = 0;
	public final int PotionPotency = 0;
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