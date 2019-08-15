package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirSuctionConfig extends AbilityConfig {
	
	public final long Cooldown = 0;
	public final int AnimationParticleAmount = 0;
	public final int SelectionParticleAmount = 0;
	public final double PushFactor = 0;
	public final double Speed = 0;
	public final double Range = 0;
	public final double SelectionRange = 0;
	public final double Radius = 0;
	
	public final double AvatarState_PushFactor = 0;
	
	public AirSuctionConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirSuction";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}