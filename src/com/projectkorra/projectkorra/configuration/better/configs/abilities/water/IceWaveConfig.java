package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class IceWaveConfig extends AbilityConfig {

	public final long Cooldown = 0;
	
	public final double ThawRadius = 0;
	public final double Damage = 0;
	public final boolean RevertSphere = true;
	public final long RevertSphereTime = 0;
	
	public final double AvatarState_Damage = 0;
	
	public IceWaveConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "IceWave";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water", "Combos" };
	}

}