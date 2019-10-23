package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthSmashConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final long ChargeTime = 2000;
	public final long Duration = 10000;
	public final int RequiredBendableBlocks = 10;
	public final int MaxBlocksToPassThrough = 3;
	public final double Damage = 4;
	public final double Knockback = 1.0;
	public final double Knockup = 0.15;
	public final double SelectRange = 15;
	public final long LiftAnimationInterval = 25;
	public final double ShootRange = 35;
	public final long ShootAnimationInterval = 25;
	
	public final long AvatarState_Cooldown = 1000;
	public final long AvatarState_ChargeTime = 500;
	public final double AvatarState_SelectRange = 15;
	public final double AvatarState_Damage = 8;
	public final double AvatarState_Knockback = 2.0;
	public final double AvatarState_ShootRange = 35;
	
	public final FlightConfig FlightConfig = new FlightConfig();
	
	public final GrabConfig GrabConfig = new GrabConfig();
	
	public static class FlightConfig {
		
		public final boolean Enabled = true;
		
		public final double Speed = .72;
		public final long Duration = 5000;
		public final long AnimationInterval = 25;
		public final double DetectionRadius = 3;
		
		public final double AvatarState_Speed = .9;
		public final long AvatarState_Duration = 20000;
		
	}
	
	public static class GrabConfig {
		
		public final boolean Enabled = true;
		
		public final double Range = 8;
		public final double DetectionRadius = 4;
		
		public final double AvatarState_Range = 15;
		
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