package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireBurstConfig extends AbilityConfig {

	public final long Cooldown = 4500;
	public final long ChargeTime = 2500;
	public final boolean Ignite = true;
	public final double Damage = 4;
	public final double Range = 20;
	public final double AngleTheta = 10;
	public final double AnglePhi = 10;
	public final double ParticlesPercentage = 5;
	
	public final long AvatarState_Cooldown = 2000;
	public final long AvatarState_ChargeTime = 1000;
	public final double AvatarState_Damage = 6;
	
	public FireBurstConfig() {
		super(true, "FireBurst is an advanced firebending technique that has a large range and the potential to deal immense damage. It's incredibly useful when surrounded by lots of mobs, to damage them all at once.", "Hold sneak until you see particles and then release sneak to send out a sphere of fire expanding outwards, damaging anything it hits. Additionally, you can left click instead of releasing sneak to send the fire burst into one direction only.");
	}

	@Override
	public String getName() {
		return "FireBurst";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}