package com.projectkorra.projectkorra.configuration.configs.abilities.chi;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class SmokescreenConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final int Duration = 3000;
	public final double Radius = 3;
	
	public SmokescreenConfig() {
		super(true, "Smokescreen, if used correctly, can serve as a defensive and offensive ability for Chiblockers. When used, a smoke bomb is fired which will blind anyone within a small radius of the explosion, allowing you to either get away, or move in for the kill.", "Left click and a smoke bomb will be fired in the direction you're looking.");
	}

	@Override
	public String getName() {
		return "Smokescreen";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Chi" };
	}

}