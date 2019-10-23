package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class JetBlazeConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Duration = 0;
	public final double Damage = 0;
	public final double Speed = 0;
	public final double FireTicks = 0;
	
	public final double AvatarState_Damage = 0;
	public final double AvatarState_FireTicks = 0;
	
	public JetBlazeConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "JetBlaze";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire", "Combos" };
	}

}