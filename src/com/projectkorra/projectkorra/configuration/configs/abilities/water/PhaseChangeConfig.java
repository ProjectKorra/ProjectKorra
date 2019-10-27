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
		super(true, "PhaseChange is one of the most useful utility moves that a waterbender possess. This ability is better used when fighting, allowing you to create a platform on water that you can fight on and being territorial by manipulating your environment. It's also useful for travelling across seas.", "\n" + "(Melt) To melt ice, hold sneak while looking at an ice block." + "\n" + "(Freeze) To freeze water and turn it into ice, simply left click at water. This ice will stay so long as you are in range, otherwise it will revert back to water. This only freezes the top layer of ice.");
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