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
	Extraction, MetalClips, Smokescreen, Combustion, LavaFlow, Suffocate, IceBlast, WarriorStance, AcrobatStance, QuickStrike, SwiftKick, EarthSmash, Flight, WaterArms, SandSpout, PlantArmor;

	private enum AirbendingAbilities {
		AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter, AirSpout, AirBurst, Suffocate, Flight;
	}

	private enum WaterbendingAbilities {
		WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout, IceSpike, IceBlast, OctopusForm, Torrent, WaterArms, PlantArmor;

	}

	private enum EarthbendingAbilities {
		Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense, EarthArmor, Shockwave, Extraction, MetalClips, LavaFlow, EarthSmash, SandSpout;
	}

	private enum FirebendingAbilities {
		HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning, FireBurst, FireShield, Combustion;
	}

	private enum ChiblockingAbilities {
		HighJump, RapidPunch, Paralyze, Smokescreen, WarriorStance, AcrobatStance, QuickStrike, SwiftKick;
	}
	
	private enum FlightAbilities
	{
		Flight;
	}
	
	private enum SpiritualProjectionAbilities
	{
		;
	}
	
	private enum CombustionbendingAbilities
	{
		Combustion;
	}
	
	private enum LightningbendingAbilities
	{
		Lightning;
	}
	
	private enum LavabendingAbilities
	{
		LavaFlow;
	}
	
	private enum MetalbendingAbilities
	{
		Extraction, MetalClips;
	}
	
	private enum SandbendingAbilities
	{
		SandSpout;
	}
	
	private enum HealingAbilities
	{
		HealingWaters;
	}
	
	private enum IcebendingAbilities
	{
		PhaseChange, IceBlast, IceSpike;
	}
	
	private enum BloodbendingAbilities
	{
		Bloodbending;
	}
	
	private enum PlantbendingAbilities
	{
		PlantArmor;
	}
	
	private enum MultiAbilities
	{
		WaterArms;
	}
	
	public static boolean isFlightAbility(String ability)
	{
		for(FlightAbilities a : FlightAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isSpiritualProjectionAbility(String ability)
	{
		for(SpiritualProjectionAbilities a : SpiritualProjectionAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isCombustionbendingAbility(String ability)
	{
		for(CombustionbendingAbilities a : CombustionbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isLightningbendingAbility(String ability)
	{
		for(LightningbendingAbilities a : LightningbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isLavabendingAbility(String ability)
	{
		for(LavabendingAbilities a : LavabendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isMetalbendingAbility(String ability)
	{
		for(MetalbendingAbilities a : MetalbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isSandbendingAbility(String ability)
	{
		for(SandbendingAbilities a : SandbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isHealingAbility(String ability)
	{
		for(HealingAbilities a : HealingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isIcebendingAbility(String ability)
	{
		for(IcebendingAbilities a : IcebendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isBloodbendingAbility(String ability)
	{
		for(BloodbendingAbilities a : BloodbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isPlantbendingAbility(String ability)
	{
		for(PlantbendingAbilities a : PlantbendingAbilities.values())
			if(a.name().equalsIgnoreCase(ability)) return true;
		return false;
	}
	
	public static boolean isStockAbility(String ability) {
		for (StockAbilities a: StockAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability)) return true;
		}
		return false;
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
	
	public static boolean isMultiAbility(StockAbilities ability) {
		for(MultiAbilities a: MultiAbilities.values()) {
			if (a.name().equalsIgnoreCase(ability.name())) return true;
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
