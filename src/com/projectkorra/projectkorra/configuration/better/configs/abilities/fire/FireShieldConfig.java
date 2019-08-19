package com.projectkorra.projectkorra.configuration.better.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class FireShieldConfig extends AbilityConfig {

	public final DiscConfig DiscConfig = new DiscConfig();
	
	public final ShieldConfig ShieldConfig = new ShieldConfig();
	
	public static class DiscConfig {
		
		public final long Cooldown = 0;
		public final long Duration = 0;
		public final double Radius = 0;
		public final double FireTicks = 0;
		
	}
	
	public static class ShieldConfig {
		
		public final long Cooldown = 0;
		public final long Duration = 0;
		public final double Radius = 0;
		public final double FireTicks = 0;
		
	}
	
	public FireShieldConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "FireShield";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}