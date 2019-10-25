package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TremorsenseConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final byte LightThreshold = 7;
	public final int MaxDepth = 12;
	public final int StickyRange = 5;
	public final int Radius = 1;
	
	public TremorsenseConfig() {
		super(true, "This is a pure utility ability for earthbenders. If you are in an area of low-light and are standing on top of an earthbendable block, this ability will automatically turn that block into glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out as you have lost contact with the earth and cannot 'see' until you can touch earth again. Additionally, if you click with this ability selected, smoke will appear above nearby earth with pockets of air beneath them.", "Simply left click while on an earthbendable block.");
	}

	@Override
	public String getName() {
		return "Tremorsense";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}