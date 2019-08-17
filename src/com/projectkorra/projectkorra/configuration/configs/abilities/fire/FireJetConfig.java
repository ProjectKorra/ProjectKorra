package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireJetConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Speed = 0;
	public final boolean ShowGliding = true;
	
	public final boolean AvatarState_Toggle = true;
	
	public FireJetConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "FireJet";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}