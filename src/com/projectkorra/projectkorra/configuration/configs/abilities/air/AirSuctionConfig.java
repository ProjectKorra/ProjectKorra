package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSuctionConfig extends AbilityConfig {
	
	public final long Cooldown = 500;
	public final int AnimationParticleAmount = 5;
	public final int SelectionParticleAmount = 5;
	public final double PushFactor_Self = 2.0;
	public final double PushFactor_Others = 1.3;
	public final double Speed = 25;
	public final double Range = 20;
	public final double SelectionRange = 10;
	public final double Radius = 1.5;
	
	public final double AvatarState_PushFactor = 3.5;
	
	public AirSuctionConfig() {
		super(true, "AirSuction is a basic ability that allows you to manipulation an entity's movement. It can be used to bring someone back to you when they're running away, or even to get yourself to great heights.", "\"\\n\" + \"(Pull) Left click while aiming at a target to pull them towards you.\" + \"\\n\" + \"(Manipulation) Sneak to select a point and then left click at a target or yourself to send you or your target to the point that you selected.");
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
