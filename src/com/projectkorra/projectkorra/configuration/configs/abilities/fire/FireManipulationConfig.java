package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireManipulationConfig extends AbilityConfig {

	public final StreamConfig StreamConfig = new StreamConfig();
	
	public final ShieldConfig ShieldConfig = new ShieldConfig();
	
	public static class StreamConfig {
		
		public final long Cooldown = 0;
		public final double Range = 0;
		public final double Damage = 0;
		public final double Speed = 0;
		public final int Particles = 0;
		
	}
	
	public static class ShieldConfig {
		
		public final long Cooldown = 0;
		public final long MaxDuration = 0;
		public final double Range = 0;
		public final double Damage = 0;
		public final int Particles = 0;
		
	}
	
	public FireManipulationConfig() {
		super(false, "", "");
	}

	@Override
	public String getName() {
		return "FireManipulation";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}