package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class CollapseConfig extends AbilityConfig {

	public final int SelectRange = 0;
	public final double Speed = 0;
	
	public final ColumnConfig ColumnConfig = new ColumnConfig();
	
	public final WallConfig WallConfig = new WallConfig();
	
	public static class ColumnConfig {
		
		public final long Cooldown = 0;
		public final int Height = 0;
		
		public final int AvatarState_Height = 0;
		
	}
	
	public static class WallConfig {
		
		public final long Cooldown = 0;
		public final int Height = 0;
		public final double Radius = 0;
		
		public final int AvatarState_Height = 0;
		
	}
	
	public CollapseConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Collapse";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}