package com.projectkorra.projectkorra.configuration.better.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AirBlastConfig extends AbilityConfig {
	
	public final long Cooldown = 0;
	public final int AnimationParticleAmount = 0;
	public final int SelectionParticleAmount = 0;
	public final double PushFactor_Self = 0;
	public final double PushFactor_Others = 0;
	public final double Speed = 0;
	public final double Range = 0;
	public final double SelectionRange = 0;
	public final double Radius = 0;
	
	public final boolean CanFlickLevers = true;
	public final boolean CanOpenDoors = true;
	public final boolean CanPushButtons = true;
	public final boolean CanCoolLava = true;
	
	public final double AvatarState_PushFactor_Self = 0;
	public final double AvatarState_PushFactor_Others = 0;
	
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