package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirBlastConfig extends AbilityConfig {
	
	public final long Cooldown = 500;
	public final int AnimationParticleAmount = 5;
	public final int SelectionParticleAmount = 5;
	public final double PushFactor_Self = 2.5;
	public final double PushFactor_Others = 2.5;
	public final double Speed = 25;
	public final double Range = 20;
	public final double SelectionRange = 10;
	public final double Radius = 1.5;
	
	public final boolean CanFlickLevers = true;
	public final boolean CanOpenDoors = true;
	public final boolean CanPushButtons = true;
	public final boolean CanCoolLava = true;
	
	public final double AvatarState_PushFactor_Self = 3.0;
	public final double AvatarState_PushFactor_Others = 3.0;
	
	public AirBlastConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AirBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}