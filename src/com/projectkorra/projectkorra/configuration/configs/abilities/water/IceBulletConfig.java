package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IceBulletConfig extends AbilityConfig {

	public final long Cooldown = 6000;
	public final long ShotCooldown = 100;
	public final long ShootTime = 4000;
	public final double Range = 30;
	public final double Radius = 4;
	public final double Damage = 2;
	public final int MaxShots = 40;
	public final double AnimationSpeed = 25;
	
	public final long AvatarState_Cooldown = 3000;
	
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