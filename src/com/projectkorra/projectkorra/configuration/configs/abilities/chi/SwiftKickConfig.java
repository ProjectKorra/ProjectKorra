package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class SwiftKickConfig extends AbilityConfig {

	public final long Cooldown = 2500;
	public final double Damage = 2;
	public final double ChiBlockChance = 75;
	
	public SwiftKickConfig() {
		super(true, "SwiftKick allows a chiblocker to swiftly kick an enemy, potentially blocking their chi.", "Jump and left click on a player to swift kick them.");
	}

	@Override
	public String getName() {
		return "SwiftKick";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}