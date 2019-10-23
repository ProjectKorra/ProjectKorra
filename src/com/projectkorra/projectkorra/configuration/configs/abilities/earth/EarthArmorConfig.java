package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthArmorConfig extends AbilityConfig {

	public final long Cooldown = 10000;
	public final long MaxDuration = 7500;
	public final double SelectRange = 15;
	public final int GoldHearts = 4;
	
	public final long AvatarState_Cooldown = 3000;
	public final int AvatarState_GoldHearts = 8;
	
	public EarthArmorConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthArmor";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}