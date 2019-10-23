package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class IceSpikeConfig extends AbilityConfig {

	public final long Cooldown = 2000;//
	public final int Height = 3;//
	public final double Damage = 2;//
	public final double Range = 20;//
	public final double Speed = 25;//
	public final double Push = .5;//
	
	public final long SlowCooldown = 2000;
	public final int SlowPower = 1;
	public final int SlowDuration = 1000;
	
	public final double AvatarState_Damage = 3;
	public final double AvatarState_Range = 20;
	public final int AvatarState_Height = 3;
	public final double AvatarState_Push = 1;
	
	public final int AvatarState_SlowPower = 2;
	public final int AvatarState_SlowDuration = 2000;
	
	public final BlastConfig BlastConfig = new BlastConfig();
	
	public final FieldConfig FieldConfig = new FieldConfig();
	
	public static class BlastConfig {
		
		public final long Cooldown = 1000;
		public final long Interval = 100;
		public final double Damage = 2;
		public final double Range = 25;
		public final double CollisionRadius = 1.5;
		public final double DeflectRange = 15;
		
		public final double ProjectileRange = 25;
		
		public final long SlowCooldown = 1000;
		public final int SlowPotency = 1;
		public final int SlowDuration = 500;
		
		public final double AvatarState_Damage = 3;
		public final double AvatarState_Range = 20;
		
		public final int AvatarState_SlowPotency = 2;
		public final int AvatarState_SlowDuration = 2000;
		
	}
	
	public static class FieldConfig {
		
		public final long Cooldown = 3000;
		public final double Damage = 4;
		public final double Radius = 9;
		public final double Knockup = .5;
		
		public final double AvatarState_Damage = 5;
		public final double AvatarState_Radius = 20;
		
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