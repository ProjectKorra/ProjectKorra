package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class RaiseEarthConfig extends AbilityConfig {

	public final ColumnConfig ColumnConfig = new ColumnConfig();
	
	public final WallConfig WallConfig = new WallConfig();
	
	public static class ColumnConfig {
		
		public final long Cooldown = 0;
		public final int Height = 0;
		public final double Speed = 0;
		public final double SelectRange = 0;
		
		public final int AvatarState_Height = 0;
		
	}
	
	public static class WallConfig {
		
		public final long Cooldown = 0;
		public final int Height = 0;
		public final int Width = 0;
		public final int SelectRange = 0;
		
		public final int AvatarState_Height = 0;
		public final int AvatarState_Width = 0;
		
	}
	
	public RaiseEarthConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "RaiseEarth";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}