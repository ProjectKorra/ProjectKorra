package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class MetalClipsConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Range = 0;
	public final double Damage = 0;
	public final boolean ThrowEnabled = true;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Range = 0;
	
	public final CrushConfig CrushConfig = new CrushConfig();
	
	public final MagnetConfig MagnetConfig = new MagnetConfig();
	
	public static class CrushConfig {
		
		public final long Cooldown = 0;
		public final double Damage = 0;
		
		public final double AvatarState_Damage = 0;
		
	}
	
	public static class MagnetConfig {
		
		public final long Cooldown = 0;
		public final double Range = 0;
		public final double Speed = 0;
		
	}
	
	public MetalClipsConfig() {
		super(true, "", "");
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