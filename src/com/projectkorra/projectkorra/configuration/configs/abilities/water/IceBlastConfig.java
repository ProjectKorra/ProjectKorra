package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IceBlastConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final long Interval = 50;
	public final double CollisionRadius = 1.5;
	public final double Range = 25;
	public final double Damage = 3;
	public final double DeflectRange = 15;
	public final boolean AllowSnow = true;
	
	public final long AvatarState_Cooldown = 500;
	public final double AvatarState_Damage = 3;
	public final double AvatarState_Range = 25;
	
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