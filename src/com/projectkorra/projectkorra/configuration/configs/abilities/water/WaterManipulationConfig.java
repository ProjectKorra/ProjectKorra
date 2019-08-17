package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterManipulationConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double SelectRange = 0;
	public final double CollisionRadius = 0;
	public final double Range = 0;
	public final double Knockback = 0;
	public final double Damage = 0;
	public final double Speed = 0;
	public final double DeflectRange = 0;
	
	public final double AvatarState_Damage = 0;
	
	public WaterManipulationConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "WaterManipulation";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}