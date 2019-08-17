package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class PhaseChangeConfig extends AbilityConfig {

	public final double SourceRange = 0;
	
	public final FreezeConfig FreezeConfig = new FreezeConfig();
	
	public final MeltConfig MeltConfig = new MeltConfig();
	
	public static class FreezeConfig {
		
		public final long Cooldown = 0;
		public final int Depth = 0;
		public final double ControlRadius = 0;
		public final int Radius = 0;
		
	}
	
	public static class MeltConfig {
		
		public final long Cooldown = 0;
		public final double Speed = 0;
		public final int Radius = 0;
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