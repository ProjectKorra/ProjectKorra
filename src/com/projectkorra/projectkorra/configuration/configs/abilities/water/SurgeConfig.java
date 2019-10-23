package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class SurgeConfig extends AbilityConfig {

	public final SurgeWaveConfig WaveConfig = new SurgeWaveConfig();
	
	public final SurgeWallConfig WallConfig = new SurgeWallConfig();
	
	public static class SurgeWaveConfig {
		
		public final long Cooldown = 3500;
		public final long Interval = 100;
		public final double Radius = 3;
		public final double Knockback = 1.0;
		public final double Knockup = .68;
		public final double MaxFreezeRadius = 3;
		public final long IceRevertTime = 10000;
		public final double Range = 20;
		public final double SelectRange = 10;
		
		public final double AvatarState_Radius = 8;
		
	}
	
	public static class SurgeWallConfig {
		
		public final long Cooldown = 0;
		public final long Interval = 100;
		public final long Duration = 0;
		public final double Range = 3;
		public final double Radius = 4;
		
		public final double AvatarState_Radius = 6;
		
	}
	
	public SurgeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Surge";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}