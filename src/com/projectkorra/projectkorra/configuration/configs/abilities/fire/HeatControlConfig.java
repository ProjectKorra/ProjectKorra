package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class HeatControlConfig extends AbilityConfig {

	public final CookConfig CookConfig = new CookConfig();
	
	public final ExtinguishConfig ExtinguishConfig = new ExtinguishConfig();
	
	public final MeltConfig MeltConfig = new MeltConfig();
	
	public final SolidifyConfig SolidifyConfig = new SolidifyConfig();
	
	public static class CookConfig {
		
		public final long Interval = 0;
		
	}
	
	public static class ExtinguishConfig {
		
		public final long Cooldown = 0;
		public final double Radius = 0;
		
	}
	
	public static class MeltConfig {
		
		public final double Range = 0;
		public final double Radius = 0;
		
	}
	
	public static class SolidifyConfig {
		
		public final double MaxRadius = 0;
		public final double Range = 0;
		public final boolean Revert = true;
		public final long RevertTime = 0;
		
	}
	
	public HeatControlConfig() {
		super(true, "HeatControl is a fundamental firebending technique that allows the firebender to control and manipulate heat. This ability is extremely useful for ensuring that you're protected from your own fire and fire from that of other firebenders. It's also offers utility by melting ice or cooking food.", "\n" + "(Melt) To melt ice, simply left click while looking at ice." + "\n" + "(Solidify) To solidify lava, hold sneak while looking at lava while standing still and it will start to solidify the lava pool you're looking at." + "\n" + "(Extinguish) To extinguish nearby fire or yourself, simply tap sneak." + "\n" + "(Cook) To cook food, place the raw food on your HeatControl slot and hold sneak. The food will then begin to cook.");
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}