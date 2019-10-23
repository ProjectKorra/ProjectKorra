package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterSpoutConfig extends AbilityConfig {

	public final long Cooldown = 100;
	public final boolean UseParticles = false;
	public final boolean EnableBlockSpiral = true;
	public final double Height = 16;
	public final long Duration = 0;
	public final long Interval = 50;
	
	public final WaterSpoutWaveConfig WaveConfig = new WaterSpoutWaveConfig();
	
	public static class WaterSpoutWaveConfig {
		
		public final boolean Enabled = true;
		public final long Cooldown = 5500;
		public final boolean AllowPlantSource = false;
		public final double Radius = 3;
		public final double WaveRadius = 3;
		public final double AnimationSpeed = 25;
		public final double SelectRange = 5;
		public final double Speed = .65;
		public final long ChargeTime = 400;
		public final long FlightDuration = 4000;
		
		public final long AvatarState_FlightDuration = 20000;
		
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