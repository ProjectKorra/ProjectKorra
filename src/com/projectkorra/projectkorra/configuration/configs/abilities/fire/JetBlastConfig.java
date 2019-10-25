package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class JetBlastConfig extends AbilityConfig {

	public final long Cooldown = 8000;
	public final long Duration = 5500;
	public final double Speed = 0.725;
	
	public JetBlastConfig() {
		super(true, "Create an explosive blast that propels your FireJet at higher speeds.", "FireJet (Tap Shift) > FireJet (Tap Shift) > FireShield (Tap Shift) > FireJet");
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