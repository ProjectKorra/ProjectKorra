package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class WaterArmsConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int InitialLength = 0;
	public final double SourceGrabRange = 0;
	public final boolean AllowPlantSource = true;
	public final int MaxAttacks = 0;
	public final int MaxIceShots = 0;
	public final int MaxAlternateUsage = 0;
	
	public final boolean LightningVulnerability = true;
	public final boolean LightningInstaKill = true;
	public final double LightningDamage = 0;
	
	public final String SneakMessage = "";
	
	public final FreezeConfig FreezeConfig = new FreezeConfig();
	
	public final SpearConfig SpearConfig = new SpearConfig();
	
	public final WhipConfig WhipConfig = new WhipConfig();
	
	public static class FreezeConfig {
		
		public final long UsageCooldown = 0;
		public final boolean UsageCooldownEnabled = true;
		
		public final int Range = 0;
		public final double Damage = 0;
		
	}
	
	public static class SpearConfig {
		
		public final long UsageCooldown = 0;
		public final boolean UsageCooldownEnabled = true;
		
		public final int Length = 0;
		
		public final double Damage = 0;
		public final boolean DamageEnabled = true;
		
		public final int RangeDay = 0;
		public final int RangeNight = 0;
		public final int RangeFullMoon = 0;
		
		public final int SphereRadiusDay = 0;
		public final int SphereRadiusNight = 0;
		public final int SphereRadiusFullMoon = 0;
		
		public final long DurationDay = 0;
		public final long DurationNight = 0;
		public final long DurationFullMoon = 0;
		
	}
	
	public static class WhipConfig {
		
		public final boolean UsageCooldownEnabled = true;
		
		public final int MaxLengthDay = 0;
		public final int MaxLengthWeak = 0;
		public final int MaxLengthNight = 0;
		public final int MaxLengthFullMoon = 0;
		
		public final long UsageCooldownPunch = 0;
		
		public final int PunchLengthDay = 0;
		public final int PunchLengthNight = 0;
		public final int PunchLengthFullMoon = 0;
		
		public final double PunchDamage = 0;
		
		public final long UsageCooldownGrab = 0;
		
		public final long GrabDuration = 0;
		
		public final long UsageCooldownPull = 0;
		
		public final double PullFactor = 0;
		
		public final long UsageCooldownGrapple = 0;
		
		public final boolean GrappleRespectRegions = true;
		
	}
	
	public WaterArmsConfig() {
		super(true, "One of the most diverse moves in a Waterbender's arsenal, this move creates tendrils \" + \"of water from the players arms to emulate their actual arms. It has the potential to do a variety of things that can either do mass amounts of damage, or used for mobility.", "To activate this ability, tap sneak at a water source. Additionally, to de-activate this ability, hold sneak and left click." + "\n" + "(Pull) Left click at a target and your arms will expand outwards, pulling entities towards you if they're in range." + "\n" + "(Punch) Left click and one arm will expand outwards, punching anyone it hits and dealing damage." + "\n" + "(Grapple) Left click to send your arms forward, pulling you to whatever surface they land on." + "\n" + "(Grab) Left click to grab an entity that's in range. They will then be controlled and moved in whatever direction you look. Additionally, if you left click again you can throw the target that you're controlling." + "\n" + "(Freeze) Left click to rapidly fire ice blasts at a target, damaging the target and giving them slowness." + "\n" + "(Spear) Left click to send an ice spear out, damaging and freezing whoever it hits in ice blocks.");
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}