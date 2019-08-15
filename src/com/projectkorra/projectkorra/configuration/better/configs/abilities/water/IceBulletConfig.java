package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class IceBulletConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ShotCooldown = 0;
	public final long ShootTime = 0;
	public final double Range = 0;
	public final double Radius = 0;
	public final double Damage = 0;
	public final int MaxShots = 0;
	public final double AnimationSpeed = 0;
	
	public final long AvatarState_Cooldown = 0;
	
	public IceBulletConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "IceBullet";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water", "Combos" };
	}

}