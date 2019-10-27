package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class QuickStrikeConfig extends AbilityConfig {

	public final long Cooldown = 1000;
	public final double Damage = 2;
	public final double ChiBlockChance = 35;
	
	public QuickStrikeConfig() {
		super(true, "QuickStrike enables a chiblocker to quickly strike an enemy, potentially blocking their chi.", "Left click on a player to quick strike them.");
	}

	@Override
	public String getName() {
		return "QuickStrike";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}