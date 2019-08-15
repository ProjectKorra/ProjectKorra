package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class OctopusFormConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final long FormDelay = 0;
	public final double SelectionRange = 0;
	public final double Damage = 0;
	public final double AttackRange = 0;
	public final long UsageCooldown = 0;
	public final double Knockback = 0;
	public final double Radius = 0;
	public final double AngleIncrement = 0;
	
	public final double AvatarState_Damage = 0;
	public final double AvatarState_AttackRange = 0;
	public final double AvatarState_Knockback = 0;
	public final double AvatarState_Radius = 0;
	
	public OctopusFormConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "OctopusForm";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}