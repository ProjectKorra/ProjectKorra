package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class RaiseEarthConfig extends AbilityConfig {

	public final ColumnConfig ColumnConfig = new ColumnConfig();
	
	public final WallConfig WallConfig = new WallConfig();
	
	public static class ColumnConfig {
		
		public final long Cooldown = 1000;
		public final int Height = 8;
		public final double Speed = 8;
		public final double SelectRange = 15;
		
		public final int AvatarState_Height = 12;
		
	}
	
	public static class WallConfig {
		
		public final long Cooldown = 3500;
		public final int Height = 6;
		public final int Width = 6;
		public final int SelectRange = 15;
		
		public final int AvatarState_Height = 50;
		public final int AvatarState_Width = 50;
		
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