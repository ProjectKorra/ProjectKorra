package com.projectkorra.projectkorra.configuration.better.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class HeatControlConfig extends AbilityConfig {

	public final CookConfig CookConfig = new CookConfig();
	
	public final ExtinguishConfig ExtinguishConfig = new ExtinguishConfig();
	
	public final MeltConfig MeltConfig = new MeltConfig();
	
	public final SolidifyConfig SolidifyConfig = new SolidifyConfig();
	
	public static class CookConfig {
		
		public final long Interval = 0;
		
	}
	
	public static class ExtinguishConfig {
		
		public final long Cooldown = 0;
		public final double Radius = 0;
		
	}
	
	public static class MeltConfig {
		
		public final double Range = 0;
		public final double Radius = 0;
		
	}
	
	public static class SolidifyConfig {
		
		public final double MaxRadius = 0;
		public final double Range = 0;
		public final boolean Revert = true;
		public final long RevertTime = 0;
		
	}
	
	public HeatControlConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}