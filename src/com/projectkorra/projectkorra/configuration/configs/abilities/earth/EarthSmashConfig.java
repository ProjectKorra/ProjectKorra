package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthSmashConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final long ChargeTime = 2000;
	public final long Duration = 10000;
	public final int RequiredBendableBlocks = 10;
	public final int MaxBlocksToPassThrough = 3;
	public final double Damage = 4;
	public final double Knockback = 1.0;
	public final double Knockup = 0.15;
	public final double SelectRange = 15;
	public final long LiftAnimationInterval = 25;
	public final double ShootRange = 35;
	public final long ShootAnimationInterval = 25;
	
	public final long AvatarState_Cooldown = 1000;
	public final long AvatarState_ChargeTime = 500;
	public final double AvatarState_SelectRange = 15;
	public final double AvatarState_Damage = 8;
	public final double AvatarState_Knockback = 2.0;
	public final double AvatarState_ShootRange = 35;
	
	public final FlightConfig FlightConfig = new FlightConfig();
	
	public final GrabConfig GrabConfig = new GrabConfig();
	
	public static class FlightConfig {
		
		public final boolean Enabled = true;
		
		public final double Speed = .72;
		public final long Duration = 5000;
		public final long AnimationInterval = 25;
		public final double DetectionRadius = 3;
		
		public final double AvatarState_Speed = .9;
		public final long AvatarState_Duration = 20000;
		
	}
	
	public static class GrabConfig {
		
		public final boolean Enabled = true;
		
		public final double Range = 8;
		public final double DetectionRadius = 4;
		
		public final double AvatarState_Range = 15;
		
	}
	
	public EarthSmashConfig() {
		super(true, "EarthSmash is an advanced earthbending technique that has lots of utility. It can be comboed with abilities such as Shockwave, but also be used for mobility and to produce high damage. EarthSmash is great for escaping when at low health.", "(Smash) Hold sneak until particles appear, then release sneak while looking at an earthbendable block which will raise an earth boulder. Then, hold sneak while looking at this boulder to control it. Left click to send the bounder in the direction you're facing, damanging entities and knocking them back.\" + \"\\n\" + \"(Ride) After you have created an earth boulder, hold sneak and right click on the boulder to ride it. You will now ride the boulder in whatever direction you look. Additionally, you can ride the boulder by going on top of it and holding sneak. If you come into contact with an entity while riding the boulder, it will drag them along with you. If you left go of sneak, the ability will cancel.");
	}

	@Override
	public String getName() {
		return "EarthSmash";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}