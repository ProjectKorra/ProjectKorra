package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class MetalClipsConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final long Duration = 7500;
	public final double Range = 20;
	public final double Damage = 1;
	public final boolean ThrowEnabled = true;
	
	public final long AvatarState_Cooldown = 1000;
	public final double AvatarState_Range = 20;
	
	public final CrushConfig CrushConfig = new CrushConfig();
	
	public final MagnetConfig MagnetConfig = new MagnetConfig();
	
	public static class CrushConfig {
		
		public final long Cooldown = 2000;
		public final double Damage = 2;
		
		public final double AvatarState_Damage = 4;
		
	}
	
	public static class MagnetConfig {
		
		public final long Cooldown = 0;
		public final double Range = 20;
		public final double Speed = .5;
		
	}
	
	public MetalClipsConfig() {
		super(true, "MetalClips is an advanced metalbending ability that allows you to take control of a fight. It gives the metalbender the ability to control an entity, create space between them and a player and even added utility.", "(Clips) This ability requires iron ingots in your inventory. Left click to throw an ingot at an entity, dealing damage to them. This ingot will form into armor, wrapping itself around the entity. Once enough armor pieces are around the entity, you can then control them. To control them, hold sneak while looking at them and then they will be moved in the direction you look. Additionally, you can release sneak to throw them in the direction you're looking.\" + \"\\n\" + \"(Magnet) Hold sneak with this ability to pull iron ingots towards you.\"");
	}

	@Override
	public String getName() {
		return "MetalClips";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}