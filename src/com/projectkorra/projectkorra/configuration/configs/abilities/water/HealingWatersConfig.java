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
		super(true, "HealingWaters is an advanced waterbender skill that allows the player to heal themselves or others from the damage they've taken. If healing another player, you must continue to look at them to channel the ability.", "Hold sneak to begin healing yourself or right click while sneaking to begin healing another player. You or the player must be in water and damaged for this ability to work, or you need to have water bottles in your inventory.");
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