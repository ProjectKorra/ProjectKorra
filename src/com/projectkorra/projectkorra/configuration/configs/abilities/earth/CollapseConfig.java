package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class CollapseConfig extends AbilityConfig {

	public final int SelectRange = 20;
	public final double Speed = 8;
	
	public final ColumnConfig ColumnConfig = new ColumnConfig();
	
	public final WallConfig WallConfig = new WallConfig();
	
	public static class ColumnConfig {
		
		public final long Cooldown = 1000;
		public final int Height = 10;
		
		public final int AvatarState_Height = 15;
		
	}
	
	public static class WallConfig {
		
		public final long Cooldown = 4500;
		public final int Height = 8;
		public final double Radius = 10;
		
		public final int AvatarState_Height = 12;
		
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