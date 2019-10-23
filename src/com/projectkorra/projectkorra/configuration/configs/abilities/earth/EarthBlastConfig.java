package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthBlastConfig extends AbilityConfig {

	public final long Cooldown = 500;
	public final double DeflectRange = 25;
	public final double CollisionRadius = 1.5;
	public final double Range = 30;
	public final double Damage = 2;
	public final double Speed = 25;
	public final boolean CanHitSelf = true;
	public final double PushFactor = .5;
	public final double SelectRange = 20;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Damage = 5;
	
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