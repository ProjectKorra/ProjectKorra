package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirShieldConfig extends AbilityConfig {

	public final long Cooldown = 7000;
	public final double MaxRadius = 5;
	public final double InitialRadius = 1;
	public final long Duration = 10000;
	public final double Speed = 25;
	public final int Streams = 5;
	public final int AnimationParticleAmount = 5;
	public final boolean DynamicCooldown = false;
	
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