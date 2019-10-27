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
		super(true, "This ability is a basic earthbending ability that allows the earthbender great utility. It allows them to control earth blocks by compressing earth. Players and mobs can be trapped and killed if earth is collapsed and they're stuck inside it, meaning this move is deadly when in cave systems.", "Left click an earthbendable block. If there's space under that block, it will be collapsed. Alternatively, you can tap sneak to collapse multiple blocks at a time.");
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