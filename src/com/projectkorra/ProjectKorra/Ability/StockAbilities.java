package com.projectkorra.ProjectKorra.Ability;

public enum StockAbilities {

	AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst,
	
	Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave,
	
	HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield,
	
	WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, OctopusForm, Torrent,
	
	HighJump, RapidPunch, Paralyze,
	
	AvatarState;
	
	private enum AirbendingAbilities {
		AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst;
	}
	
	private enum WaterbendingAbilities {
		WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, OctopusForm, Torrent;
	}
	
	private enum EarthbendingAbilities {
		Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave;
	}
	
	private enum FirebendingAbilities {
		HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield;
	}
	
	private enum ChiblockingAbilities {
		HighJump, RapidPunch, Paralyze;
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
}
