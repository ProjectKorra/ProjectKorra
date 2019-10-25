package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class ShockwaveConfig extends AbilityConfig {

	public final long Cooldown = 4000;
	public final long ChargeTime = 2500;
	public final double FallThreshold = 15;
	public final double Range = 20;
	public final double Damage = 4;
	public final double Knockback = 1.2;
	public final double Angle = 40;
	
	public final long AvatarState_Cooldown = 1000;
	public final long AvatarState_ChargeTime = 500;
	public final double AvatarState_Range = 40;
	public final double AvatarState_Damage = 6;
	public final double AvatarState_Knockback = 2.0;
	
	public ShockwaveConfig() {
		super(true, "Shockwave is one of the most powerful earthbending abilities. It allows the earthbender to deal mass damage to everyone around them and knock them back. It's extremely useful when fighting more than one target or if you're surrounded by mobs.", "Hold sneak until you see particles and then release sneak to send a wave of earth outwards, damaging and knocking entities back that it collides with. Additionally, instead of releasing sneak you can send a cone of earth forwards by left clicking. If you are on the Shockwave slot and you fall from a great height, your Shockwave will automatically activate.");
	}

	@Override
	public String getName() {
		return "Shockwave";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}