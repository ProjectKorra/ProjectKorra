package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class TwisterConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double Range = 0;
	public final double Speed = 0;
	public final double Height = 0;
	public final double Radius = 0;
	public final double DegreesPerParticle = 0;
	public final double HeightPerParticle = 0;
	public final long RemoveDelay = 0;
	
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Range = 0;
	
	public TwisterConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Twister";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Combos" };
	}

}