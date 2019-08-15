package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class WaterSpoutConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final boolean UseParticles = true;
	public final boolean EnableBlockSpiral = true;
	public final double Height = 0;
	public final long Duration = 0;
	public final long Interval = 0;
	
	public final WaterSpoutWaveConfig WaveConfig = new WaterSpoutWaveConfig();
	
	public static class WaterSpoutWaveConfig {
		
		public final boolean Enabled = true;
		public final long Cooldown = 0;
		public final boolean AllowPlantSource = true;
		public final double Radius = 0;
		public final double WaveRadius = 0;
		public final double AnimationSpeed = 0;
		public final double SelectRange = 0;
		public final double Speed = 0;
		public final long ChargeTime = 0;
		public final long FlightDuration = 0;
		
		public final long AvatarState_FlightDuration = 0;
		
	}
	
	public WaterSpoutConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "WaterSpout";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}