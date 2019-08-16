package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthArmorConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long MaxDuration = 0;
	public final double SelectRange = 0;
	public final int GoldHearts = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final int AvatarState_GoldHearts = 0;
	
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