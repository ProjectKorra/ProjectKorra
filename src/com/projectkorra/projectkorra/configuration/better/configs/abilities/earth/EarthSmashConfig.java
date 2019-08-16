package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthSmashConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ChargeTime = 0;
	public final long Duration = 0;
	public final int RequiredBendableBlocks = 0;
	public final int MaxBlocksToPassThrough = 0;
	public final double Damage = 0;
	public final double Knockback = 0;
	public final double Knockup = 0;
	public final double SelectRange = 0;
	public final long LiftAnimationInterval = 0;
	public final double ShootRange = 0;
	public final long ShootAnimationInterval = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_SelectRange = 0;
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Knockback = 0;
	public final double AvatarState_ShootRange = 0;
	
	public final FlightConfig FlightConfig = new FlightConfig();
	
	public final GrabConfig GrabConfig = new GrabConfig();
	
	public static class FlightConfig {
		
		public final boolean Enabled = true;
		
		public final double Speed = 0;
		public final long Duration = 0;
		public final long AnimationInterval = 0;
		public final double DetectionRadius = 0;
		
		public final double AvatarState_Speed = 0;
		public final long AvatarState_Duration = 0;
		
	}
	
	public static class GrabConfig {
		
		public final boolean Enabled = true;
		
		public final double Range = 0;
		public final double DetectionRadius = 0;
		
		public final double AvatarState_Range = 0;
		
	}
	
	public EarthSmashConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthSmash";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}