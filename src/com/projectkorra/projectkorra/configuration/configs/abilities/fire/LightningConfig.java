package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class LightningConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long ChargeTime = 0;
	public final double Damage = 0;
	public final double Range = 0;
	public final boolean SelfHitWater = true;
	public final boolean SelfHitClose = true;
	public final boolean ArcOnIce = true;
	public final double MaxArcAngle = 0;
	public final double SubArcChance = 0;
	public final double ChainArcRange = 0;
	public final double ChainArcChance = 0;
	public final double WaterArcRange = 0;
	public final double StunChance = 0;
	public final long StunDuration = 0;
	public final int MaxChainArcs = 0;
	public final int WaterArcs = 0;
	
	public final long AvatarState_Cooldown = 0;
	public final long AvatarState_ChargeTime = 0;
	public final double AvatarState_Damage = 0;
	
	public LightningConfig() {
		super(true, "Lightning is an advanced firebending technique. It allows you to create lightning and manipulate it towards a target to deal immense damage.", "\n" + "(Lightning) Hold sneak to create lightning until particles appear, then release sneak to send lightning in the direction you're looking. This deals damage to entities that it hits and has a chance to stun them for a short duration." + "\n" + "(Redirection) When someone has fired a lightning strike at you, you can hold sneak to absorb this lightning and then release sneak to fire it back.");
	}

	@Override
	public String getName() {
		return "Lightning";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}