package com.projectkorra.projectkorra.configuration.better.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class BlazeConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int Arc = 0;
	public final double Range = 0;
	public final double Speed = 0;
	
	public final RingConfig RingConfig = new RingConfig();
	
	public static class RingConfig {
		
		public final long Cooldown = 0;
		public final int Range = 0;
		public final double Angle = 0;
		
		public final int AvatarState_Range = 0;
		
	}
	
	public BlazeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}