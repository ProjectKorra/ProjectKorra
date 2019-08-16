package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthBlastConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final double DeflectRange = 0;
	public final double CollisionRadius = 0;
	public final double Range = 0;
	public final double Damage = 0;
	public final double Speed = 0;
	public final boolean CanHitSelf = true;
	public final double PushFactor = 0;
	public final double SelectRange = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Damage = 0;
	
	public EarthBlastConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}