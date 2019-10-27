package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireJetConfig extends AbilityConfig {

	public final long Cooldown = 5000;
	public final long Duration = 4000;
	public final double Speed = .7;
	public final boolean ShowGliding = true;
	
	public final boolean AvatarState_Toggle = true;
	
	public FireJetConfig() {
		super(true, "FireJet is a fundamental utility move for firebenders. It allows the firebender to blast fire behind them to propel them forward, which can prevent them from taking fall damage or to escape from deadly situations.", "Left click to propel yourself in the direction you're looking. Additionally, left click while flying to cancel the jet.");
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