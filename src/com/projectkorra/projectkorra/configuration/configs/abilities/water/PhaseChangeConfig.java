package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class PhaseChangeConfig extends AbilityConfig {

	public final double SourceRange = 30;
	
	public final FreezeConfig FreezeConfig = new FreezeConfig();
	
	public final MeltConfig MeltConfig = new MeltConfig();
	
	public static class FreezeConfig {
		
		public final long Cooldown = 500;
		public final int Depth = 1;
		public final double ControlRadius = 5;
		public final int Radius = 4;
		
	}
	
	public static class MeltConfig {
		
		public final long Cooldown = 0;
		public final double Speed = 300;
		public final int Radius = 15;
		public final boolean AllowFlow = true;
		
	}
	
	public PhaseChangeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "PhaseChange";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}