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
		super(true, "", "");
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