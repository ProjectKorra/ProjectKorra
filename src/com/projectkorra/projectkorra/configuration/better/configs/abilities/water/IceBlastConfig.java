package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class IceBlastConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Interval = 0;
	public final double CollisionRadius = 0;
	public final double Range = 0;
	public final double Damage = 0;
	public final double DeflectRange = 0;
	public final boolean AllowSnow = true;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Range = 0;
	
	public IceBlastConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "IceBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}