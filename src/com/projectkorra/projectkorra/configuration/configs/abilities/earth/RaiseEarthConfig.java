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
		super(true, "RaiseEarth is a basic yet useful utility move. It has the potential to allow the earthbender to create great escape routes by raising earth underneath them to propell themselves upwards. It also offers synergy with other moves, such as shockwave. RaiseEarth is often used to block incoming abilities.", "(Pillar) To raise a pillar of earth, left click on an earthbendable block.\" + \"\\n\" + \"(Wall) To raise a wall of earth, tap sneak on an earthbendable block.");
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