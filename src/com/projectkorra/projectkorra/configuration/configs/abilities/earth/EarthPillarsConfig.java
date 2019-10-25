package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthPillarsConfig extends AbilityConfig {

	public final long Cooldown = 3500;
	public final double Radius = 15;
	public final double Knockup = 1.2;
	public final double Damage = 4;
	public final boolean DealsDamage = true;
	public final double FallHeightThreshold = 15;
	
	public EarthPillarsConfig() {
		super(true, "Send players and entities flying into the air and possibly stunning them by raising pillars of earth under their feet, dealing damage initally as well. This combo can also be used by falling from high off the ground and landing while on the Catapult ability", "Shockwave (Tap sneak) > Shockwave (Hold sneak) > Catapult (Release sneak)");
	}

	@Override
	public String getName() {
		return "EarthPillars";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth", "Combos" };
	}

}