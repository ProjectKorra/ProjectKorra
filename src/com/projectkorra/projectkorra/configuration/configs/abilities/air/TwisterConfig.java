package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TwisterConfig extends AbilityConfig {

	public final long Cooldown = 7500;
	public final double Range = 30;
	public final double Speed = 25;
	public final double Height = 15;
	public final double Radius = 10;
	public final double DegreesPerParticle = 7;
	public final double HeightPerParticle = 1.25;
	public final long RemoveDelay = 1000;
	
	public final double AvatarState_Height = 15;
	public final double AvatarState_Range = 40;
	
	public TwisterConfig() {
		super(true, "Create a cyclone of air that travels along the ground grabbing nearby entities.", "AirShield (Tap Shift) > Tornado (Hold Shift) > AirBlast (Left Click)");
	}

	@Override
	public String getName() {
		return "Twister";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air", "Combos" };
	}

}