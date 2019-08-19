package com.projectkorra.projectkorra.configuration.better.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class JetBlastConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Speed = 0;
	
	public JetBlastConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "JetBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire", "Combos" };
	}

}