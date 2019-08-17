package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IceSpikeConfig extends AbilityConfig {

	public final long Cooldown = 0;//
	public final int Height = 0;//
	public final double Damage = 0;//
	public final double Range = 0;//
	public final double Speed = 0;//
	public final double Push = 0;//
	
	public final long SlowCooldown = 0;
	public final int SlowPower = 0;
	public final int SlowDuration = 0;
	
	public final double AvatarState_Damage = 0;
	public final double AvatarState_Range = 0;
	public final int AvatarState_Height = 0;
	public final double AvatarState_Push = 0;
	
	public final int AvatarState_SlowPower = 0;
	public final int AvatarState_SlowDuration = 0;
	
	public final BlastConfig BlastConfig = new BlastConfig();
	
	public final FieldConfig FieldConfig = new FieldConfig();
	
	public static class BlastConfig {
		
		public final long Cooldown = 0;
		public final long Interval = 0;
		public final double Damage = 0;
		public final double Range = 0;
		public final double CollisionRadius = 0;
		public final double DeflectRange = 0;
		
		public final double ProjectileRange = 0;
		
		public final long SlowCooldown = 0;
		public final int SlowPotency = 0;
		public final int SlowDuration = 0;
		
		public final double AvatarState_Damage = 0;
		public final double AvatarState_Range = 0;
		
		public final int AvatarState_SlowPotency = 0;
		public final int AvatarState_SlowDuration = 0;
		
	}
	
	public static class FieldConfig {
		
		public final long Cooldown = 0;
		public final double Damage = 0;
		public final double Radius = 0;
		public final double Knockup = 0;
		
		public final double AvatarState_Damage = 0;
		public final double AvatarState_Radius = 0;
		
	}
	
	public IceSpikeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}