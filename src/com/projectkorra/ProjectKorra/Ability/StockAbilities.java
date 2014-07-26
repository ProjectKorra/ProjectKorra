package com.projectkorra.ProjectKorra.Ability;

import java.util.Arrays;

public enum StockAbilities {

	// Old Bending
	AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst,
	
	Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave, 
	
	HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield,
	
	WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, OctopusForm, Torrent,
	
	HighJump, RapidPunch, Paralyze,
	
	AvatarState,
	
	// Project Korra
	Extraction,
	
	Energybending;
	
	private enum AirbendingAbilities {
		AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst;
	}
	
	private enum WaterbendingAbilities {
		WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, OctopusForm, Torrent;
	}
	
	private enum EarthbendingAbilities {
		Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave, Extraction;
	}
	
	private enum FirebendingAbilities {
		HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield;
	}
	
	private enum ChiblockingAbilities {
		HighJump, RapidPunch, Paralyze;
	}
	
	private enum AvatarAbilities {
		AvatarState, Energybending;
	}
	
	public static boolean isAirbending(StockAbilities ability) {
		for (AirbendingAbilities a: AirbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
		}
		return false;
	}
	
	public static boolean isWaterbending(StockAbilities ability) {
		for (WaterbendingAbilities a: WaterbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
		}
		return false;
	}
	
	public static boolean isEarthbending(StockAbilities ability) {
		for (EarthbendingAbilities a: EarthbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
		}
		return false;
	}
	
	public static boolean isFirebending(StockAbilities ability) {
		for (FirebendingAbilities a: FirebendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
		}
		return false;
	}
	
	public static boolean isChiBlocking(StockAbilities ability) {
		for (ChiblockingAbilities a: ChiblockingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
		}
		
		return false;
	}
	
	public static boolean isAvatar(StockAbilities ability) {
		for (AvatarAbilities a: AvatarAbilities.values()) {
			if(a.name().equalsIgnoreCase(ability.name())) return true;
		}
		
		return false;
	}
	
	public static StockAbilities getAbility(int index) {
		if (index == -1)
			return null;
		if (index > 41) return null;
		return Arrays.asList(StockAbilities.values()).get(index);
	}
}
