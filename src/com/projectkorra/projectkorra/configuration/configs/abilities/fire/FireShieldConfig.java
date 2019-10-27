package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireShieldConfig extends AbilityConfig {

	public final DiscConfig DiscConfig = new DiscConfig();
	
	public final ShieldConfig ShieldConfig = new ShieldConfig();
	
	public static class DiscConfig {
		
		public final long Cooldown = 0;
		public final long Duration = 0;
		public final double Radius = 1.5;
		public final double FireTicks = 1;
		
	}
	
	public static class ShieldConfig {
		
		public final long Cooldown = 0;
		public final long Duration = 0;
		public final double Radius = 3;
		public final double FireTicks = 1;
		
	}
	
	public FireShieldConfig() {
		super(true, "FireShield is a basic defensive ability that allows a firebender to block projectiles or other bending abilities. It's useful while fighting off skeletons, or while trying to block bending abilities at low health.", "Hold sneak to create a fire shield around you that will block projectiles and other bending abilities. Additionally, left click to create a temporary fire shield. If entities step inside this fire shield, they will be ignited.");
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