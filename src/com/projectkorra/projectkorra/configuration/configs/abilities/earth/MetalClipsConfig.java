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