package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class SurgeConfig extends AbilityConfig {

	public final SurgeWaveConfig WaveConfig = new SurgeWaveConfig();
	
	public final SurgeWallConfig WallConfig = new SurgeWallConfig();
	
	public static class SurgeWaveConfig {
		
		public final long Cooldown = 0;
		public final long Interval = 0;
		public final double Radius = 0;
		public final double Knockback = 0;
		public final double Knockup = 0;
		public final double MaxFreezeRadius = 0;
		public final long IceRevertTime = 0;
		public final double Range = 0;
		public final double SelectRange = 0;
		
		public final double AvatarState_Radius = 0;
		
	}
	
	public static class SurgeWallConfig {
		
		public final long Cooldown = 0;
		public final long Interval = 0;
		public final long Duration = 0;
		public final double Range = 0;
		public final double Radius = 0;
		
		public final double AvatarState_Radius = 0;
		
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