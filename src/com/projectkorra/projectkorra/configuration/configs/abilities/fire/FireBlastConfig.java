package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireBlastConfig extends AbilityConfig {

	public final long Cooldown = 1000;
	public final boolean Dissipate = true;
	public final double Damage = 2;
	public final double Range = 25;
	public final double Speed = 25;
	public final double CollisionRadius = 1.5;
	public final double FireTicks = 1;
	public final double Knockback = 0;
	
	public final double FlameParticleRadius = 1.0;
	public final double SmokeParticleRadius = 1.0;
	
	public final ChargedConfig ChargedConfig = new ChargedConfig();
	
	public static class ChargedConfig {
		
		public final long Cooldown = 3000;
		public final long ChargeTime = 1500;
		public final boolean Dissipate = true;
		public final boolean DamageBlocks = true;
		public final double CollisionRadius = 2;
		public final double Damage = 4;
		public final double Range = 30;
		public final double DamageRadius = 2;
		public final double ExplosionRadius = 2;
		public final double FireTicks = 2;
		
		public final long AvatarState_ChargeTime = 500;
		public final double AvatarState_Damage = 5;
		
	}
	
	public FireBlastConfig() {
		super(true, "FireBlast is the most fundamental bending technique of a firebender. It allows the firebender to create mass amounts of fire blasts to constantly keep damaging an entity. It's great for rapid fire successions to deal immense damage.", "\\n\" + \"(Ball) Left click to send out a ball of fire that will deal damage and knockback entities it hits. Additionally, this ability can refuel furnace power if the blast connects with a furnace.\" + \"\\n\" + \"(Blast) Hold sneak until you see particles and then release sneak to send out a powerful fire blast outwards. This deals damage and knocks back anyone it hits, while exploding on impact.");
	}

	@Override
	public String getName() {
		return "FireBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Fire" };
	}

}