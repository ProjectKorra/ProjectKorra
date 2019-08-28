package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSuctionConfig extends AbilityConfig {
	
	public final long Cooldown = 500;
	public final int AnimationParticleAmount = 5;
	public final int SelectionParticleAmount = 5;
	public final double PushFactor = 2.5;
	public final double Speed = 25;
	public final double Range = 20;
	public final double SelectionRange = 10;
	public final double Radius = 1.5;
	
	public final double AvatarState_PushFactor = 3.5;
	
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