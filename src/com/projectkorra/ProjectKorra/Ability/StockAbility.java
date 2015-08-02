package com.projectkorra.ProjectKorra.Ability;

import java.util.Arrays;

/**
 * An enum representation of all ProjectKorra core abilities.
 */
public enum StockAbility {

	// Old Bending
	AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst,

	Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave,

	HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield,

	WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, OctopusForm, Torrent,

	HighJump, RapidPunch, Paralyze,

	AvatarState,

	// Project Korra
	Extraction, MetalClips, Smokescreen, Combustion, LavaFlow, Suffocate, IceBlast, WarriorStance, AcrobatStance, QuickStrike, SwiftKick, EarthSmash, Flight, WaterArms, SandSpout, PlantArmor;

	public enum AirbendingAbilities {
		AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst, Suffocate, Flight;
	}

	public enum WaterbendingAbilities {
		WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, IceBlast, OctopusForm, Torrent, WaterArms, PlantArmor;

	}

	public enum EarthbendingAbilities {
		Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave, Extraction, MetalClips, LavaFlow, EarthSmash, SandSpout;
	}

	public enum FirebendingAbilities {
		HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield, Combustion;
	}

	public enum ChiblockingAbilities {
		HighJump, RapidPunch, Paralyze, Smokescreen, WarriorStance, AcrobatStance, QuickStrike, SwiftKick;
	}

	public enum FlightAbilities {
		Flight;
	}

	private enum SpiritualProjectionAbilities {
		;
	}

	public enum CombustionbendingAbilities {
		Combustion;
	}

	public enum LightningbendingAbilities {
		Lightning;
	}

	public enum LavabendingAbilities {
		LavaFlow;
	}

	public enum MetalbendingAbilities {
		Extraction, MetalClips;
	}

	public enum SandbendingAbilities {
		SandSpout;
	}

	public enum HealingAbilities {
		HealingWaters;
	}

	public enum IcebendingAbilities {
		PhaseChange, IceBlast, IceSpike;
	}

	public enum BloodbendingAbilities {
		Bloodbending;
	}

	public enum PlantbendingAbilities {
		PlantArmor;
	}

	public enum MultiAbilities {
		WaterArms;
	}

	public static boolean isFlightAbility(String ability) {
		for (FlightAbilities a : FlightAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isSpiritualProjectionAbility(String ability) {
		for (SpiritualProjectionAbilities a : SpiritualProjectionAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isCombustionbendingAbility(String ability) {
		for (CombustionbendingAbilities a : CombustionbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isLightningbendingAbility(String ability) {
		for (LightningbendingAbilities a : LightningbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isLavabendingAbility(String ability) {
		for (LavabendingAbilities a : LavabendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isMetalbendingAbility(String ability) {
		for (MetalbendingAbilities a : MetalbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isSandbendingAbility(String ability) {
		for (SandbendingAbilities a : SandbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isHealingAbility(String ability) {
		for (HealingAbilities a : HealingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isIcebendingAbility(String ability) {
		for (IcebendingAbilities a : IcebendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isBloodbendingAbility(String ability) {
		for (BloodbendingAbilities a : BloodbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isPlantbendingAbility(String ability) {
		for (PlantbendingAbilities a : PlantbendingAbilities.values())
			if (a.name().equalsIgnoreCase(ability))
				return true;
		return false;
	}

	public static boolean isStockAbility(String ability) {
		for (StockAbility a : StockAbility.values()) {
			if (a.name().equalsIgnoreCase(ability))
				return true;
		}
		return false;
	}

	public static boolean isAirbending(StockAbility ability) {
		for (AirbendingAbilities a : AirbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}
		return false;
	}

	public static boolean isWaterbending(StockAbility ability) {
		for (WaterbendingAbilities a : WaterbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}
		return false;
	}

	public static boolean isEarthbending(StockAbility ability) {
		for (EarthbendingAbilities a : EarthbendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}
		return false;
	}

	public static boolean isFirebending(StockAbility ability) {
		for (FirebendingAbilities a : FirebendingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}
		return false;
	}

	public static boolean isChiBlocking(StockAbility ability) {
		for (ChiblockingAbilities a : ChiblockingAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}

		return false;
	}

	public static boolean isMultiAbility(StockAbility ability) {
		for (MultiAbilities a : MultiAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name()))
				return true;
		}
		return false;
	}

	public static StockAbility getAbility(int index) {
		if (index == -1)
			return null;
		if (index > StockAbility.values().length)
			return null;
		return Arrays.asList(StockAbility.values()).get(index);
	}
}
