package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirShieldConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double MaxRadius = 0;
	public final double InitialRadius = 0;
	public final long Duration = 0;
	public final double Speed = 0;
	public final int Streams = 0;
	public final int AnimationParticleAmount = 0;
	public final boolean DynamicCooldown = true;
	
	public final boolean AvatarState_Toggle = true;
	
	public AirShieldConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirShield";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}