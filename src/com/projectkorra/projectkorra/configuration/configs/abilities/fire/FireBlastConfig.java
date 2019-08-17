package com.projectkorra.projectkorra.configuration.configs.abilities.fire;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class FireBlastConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final boolean Dissipate = true;
	public final double Damage = 0;
	public final double Range = 0;
	public final double Speed = 0;
	public final double CollisionRadius = 0;
	public final double FireTicks = 0;
	public final double Knockback = 0;
	
	public final ChargedConfig ChargedConfig = new ChargedConfig();
	
	public static class ChargedConfig {
		
		public final long Cooldown = 0;
		public final long ChargeTime = 0;
		public final boolean Dissipate = true;
		public final boolean DamageBlocks = true;
		public final double CollisionRadius = 0;
		public final double Damage = 0;
		public final double Range = 0;
		public final double DamageRadius = 0;
		public final double ExplosionRadius = 0;
		public final double FireTicks = 0;
		
		public final long AvatarState_ChargeTime = 0;
		public final double AvatarState_Damage = 0;
		
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