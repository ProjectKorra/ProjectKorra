package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterManipulationConfig extends AbilityConfig {

	public final long Cooldown = 3500;
	public final double SelectRange = 20;
	public final double CollisionRadius = 1.5;
	public final double Range = 25;
	public final double Knockback = .2;
	public final double Damage = 2;
	public final double Speed = 25;
	public final double DeflectRange = 15;
	
	public final double AvatarState_Damage = 4;
	
	public WaterManipulationConfig() {
		super(true, "WaterManipulation is a fundamental ability for waterbenders. Although it is a basic move, it allows for fast damage due to its rapid fire nature, which is incredibly useful when wanting to finish off low health targets.", "Tap sneak on a water source and left click to send a water manipulation to the point that you clicked. Additionally, you can left click again to change the direction of this move. This includes other players' WaterManipulations.");
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