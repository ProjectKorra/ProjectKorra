package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthDomeConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final int Height = 5;
	public final double Radius = 4;
	public final double Range = 25;
	
	public EarthDomeConfig() {
		super(true, "EarthDome allows earthbenders to surround themselves or another entity in earth, temporarily preventing anything from entering or escaping the dome.", "(Self) RaiseEarth (Right click) > Shockwave (Right click)\\\\n(Projection) RaiseEarth(Right click) > Shockwave (Left click)");
	}

	@Override
	public String getName() {
		return "EarthDome";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth", "Combos" };
	}

}