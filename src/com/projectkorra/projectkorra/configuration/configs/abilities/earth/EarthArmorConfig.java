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
		super(true, "This ability encases the Earthbender in armor, giving them protection. It is a fundamental earthbending technique that's used to survive longer in battles.", "Tap sneak while looking at an earthbendable block to bring those blocks towards you, forming earth armor. This ability will give you extra hearts and will be removed once those extra hearts are gone. You can disable this ability by holding sneak and left clicking with EarthArmor.");
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