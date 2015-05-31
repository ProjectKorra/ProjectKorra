package com.projectkorra.ProjectKorra.Ability;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.AbilityLoader;


public class AbilityModuleManager {

	static ProjectKorra plugin;
	public static List<AbilityModule> ability;
	private final AbilityLoader<AbilityModule> loader;

	public static HashSet<String> abilities;
    public static HashSet<String> disabledStockAbilities;
	public static List<String> waterbendingabilities;
	public static List<String> airbendingabilities;
	public static List<String> earthbendingabilities;
	public static List<String> firebendingabilities;
	public static List<String> chiabilities;
	public static HashSet<String> shiftabilities;
	public static HashMap<String, String> authors;
	public static HashSet<String> harmlessabilities;
	public static HashSet<String> igniteabilities;
	public static HashSet<String> explodeabilities;
	public static HashSet<String> metalbendingabilities;
	public static HashSet<String> earthsubabilities;
	public static HashSet<String> subabilities;
	public static HashSet<String> lightningabilities;
	public static HashSet<String> combustionabilities;
	public static HashSet<String> lavaabilities;
	public static HashSet<String> sandabilities;
	public static HashSet<String> metalabilities;
	public static HashSet<String> flightabilities;
	public static HashSet<String> spiritualprojectionabilities;
	public static HashSet<String> iceabilities;
	public static HashSet<String> healingabilities;
	public static HashSet<String> plantabilities;
	public static HashSet<String> bloodabilities;
	
	public static HashMap<String, String> descriptions;

	public AbilityModuleManager(final ProjectKorra plugin) {
		AbilityModuleManager.plugin = plugin;
		final File path = new File(plugin.getDataFolder().toString() + "/Abilities/");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new AbilityLoader<AbilityModule>(plugin, path, new Object[] {});
		abilities = new HashSet<String>();
		waterbendingabilities = new ArrayList<String>();
		airbendingabilities = new ArrayList<String>();
		earthbendingabilities = new ArrayList<String>();
		firebendingabilities = new ArrayList<String>();
		chiabilities = new ArrayList<String>();
		shiftabilities = new HashSet<String>();
		descriptions = new HashMap<String, String>();
		authors = new HashMap<String, String>();
		harmlessabilities = new HashSet<String>();
		explodeabilities = new HashSet<String>();
		igniteabilities = new HashSet<String>();
		metalbendingabilities = new HashSet<String>();
		earthsubabilities = new HashSet<String>();
		subabilities = new HashSet<String>();
		ability = loader.load(AbilityModule.class);
		disabledStockAbilities = new HashSet<String>();
		lightningabilities = new HashSet<String>();
		combustionabilities = new HashSet<String>();
		flightabilities = new HashSet<String>();
		spiritualprojectionabilities = new HashSet<String>();
		metalabilities = new HashSet<String>();
		sandabilities = new HashSet<String>();
		lavaabilities = new HashSet<String>();
		healingabilities = new HashSet<String>();
		plantabilities = new HashSet<String>();
		iceabilities = new HashSet<String>();
		bloodabilities = new HashSet<String>();
		fill();
	}

	private void fill() {

		for (StockAbilities a: StockAbilities.values()) {
			if (StockAbilities.isAirbending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					airbendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Air." + a.name() + ".Description"));
					if (a == StockAbilities.AirScooter) harmlessabilities.add(a.name());
					if (a == StockAbilities.AirSpout) harmlessabilities.add(a.name());
					if (a == StockAbilities.Tornado) shiftabilities.add(a.name());
					if (a == StockAbilities.AirSuction) shiftabilities.add(a.name());
					if (a == StockAbilities.AirSwipe) shiftabilities.add(a.name());
					if (a == StockAbilities.AirBlast) shiftabilities.add(a.name());
					if (a == StockAbilities.AirBurst) shiftabilities.add(a.name());
					if (a == StockAbilities.AirShield) shiftabilities.add(a.name());
					if (a == StockAbilities.Flight) shiftabilities.add(a.name());
					
					// Air Sub Abilities
					if (a == StockAbilities.Flight) subabilities.add(a.name());
					if (a == StockAbilities.Flight) flightabilities.add(a.name());
				}
			}
			else if (StockAbilities.isWaterbending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					waterbendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Water." + a.name() + ".Description"));
					if (a == StockAbilities.WaterSpout) harmlessabilities.add(a.name());
					if (a == StockAbilities.HealingWaters) harmlessabilities.add(a.name());
					if (a == StockAbilities.Surge) shiftabilities.add(a.name());
					if (a == StockAbilities.Bloodbending) shiftabilities.add(a.name());
					if (a == StockAbilities.PhaseChange) shiftabilities.add(a.name());
					if (a == StockAbilities.HealingWaters) shiftabilities.add(a.name());
					if (a == StockAbilities.OctopusForm) shiftabilities.add(a.name());
					if (a == StockAbilities.Torrent) shiftabilities.add(a.name());
					if (a == StockAbilities.WaterManipulation) shiftabilities.add(a.name());
					if (a == StockAbilities.WaterArms) shiftabilities.add(a.name());
					if (a == StockAbilities.IceSpike) shiftabilities.add(a.name());
					if (a == StockAbilities.IceBlast) shiftabilities.add(a.name());
					
					// Water Sub Abilities
					if (a == StockAbilities.HealingWaters) subabilities.add(a.name());
					if (a == StockAbilities.Bloodbending) subabilities.add(a.name());
					if (a == StockAbilities.PhaseChange) subabilities.add(a.name());
					if (a == StockAbilities.IceSpike) subabilities.add(a.name());
					if (a == StockAbilities.IceBlast) subabilities.add(a.name());
					
					if (a == StockAbilities.HealingWaters) healingabilities.add(a.name());
					if (a == StockAbilities.Bloodbending) bloodabilities.add(a.name());
					if (a == StockAbilities.PhaseChange) iceabilities.add(a.name());
					if (a == StockAbilities.IceSpike) iceabilities.add(a.name());
					if (a == StockAbilities.IceBlast) iceabilities.add(a.name());
				}
			}
			else if (StockAbilities.isEarthbending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					earthbendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Earth." + a.name() + ".Description"));
					if (a == StockAbilities.Tremorsense) harmlessabilities.add(a.name());
					if (a == StockAbilities.RaiseEarth) shiftabilities.add(a.name());
					if (a == StockAbilities.Collapse) shiftabilities.add(a.name());
					if (a == StockAbilities.EarthBlast) shiftabilities.add(a.name());
					if (a == StockAbilities.Shockwave) shiftabilities.add(a.name());
					if (a == StockAbilities.EarthTunnel) shiftabilities.add(a.name());
					if (a == StockAbilities.EarthGrab) shiftabilities.add(a.name());
					if (a == StockAbilities.LavaFlow) shiftabilities.add(a.name());
					if (a == StockAbilities.MetalClips) shiftabilities.add(a.name());
					if (a == StockAbilities.EarthSmash) shiftabilities.add(a.name());
					
					// Earth Sub Abilities
					if (a == StockAbilities.MetalClips) subabilities.add(a.name());
					if (a == StockAbilities.Extraction) subabilities.add(a.name());
					if (a == StockAbilities.LavaFlow) subabilities.add(a.name());
					
					if (a == StockAbilities.MetalClips) metalabilities.add(a.name());
					if (a == StockAbilities.Extraction) metalabilities.add(a.name());
					if (a == StockAbilities.LavaFlow) lavaabilities.add(a.name());
//					if (a == StockAbilities.LavaSurge) earthsubabilities.add(a.name());
					
				}
			}
			else if (StockAbilities.isFirebending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					firebendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Fire." + a.name() + ".Description"));
					if (a == StockAbilities.Illumination) harmlessabilities.add(a.name());
					if (a == StockAbilities.Blaze) igniteabilities.add(a.name());
					if (a == StockAbilities.FireBlast) explodeabilities.add(a.name());
					if (a == StockAbilities.Lightning) explodeabilities.add(a.name());
					if (a == StockAbilities.Combustion) explodeabilities.add(a.name());
					if (a == StockAbilities.HeatControl) shiftabilities.add(a.name());
					if (a == StockAbilities.Lightning) shiftabilities.add(a.name());
					if (a == StockAbilities.FireBlast) shiftabilities.add(a.name());
					if (a == StockAbilities.Blaze) shiftabilities.add(a.name());
					if (a == StockAbilities.FireBurst) shiftabilities.add(a.name());
					
					// Fire Sub Abilities
					if (a == StockAbilities.Lightning) subabilities.add(a.name());
					if (a == StockAbilities.Combustion) subabilities.add(a.name());
					
					if (a == StockAbilities.Lightning) lightningabilities.add(a.name());
					if (a == StockAbilities.Combustion) combustionabilities.add(a.name());
				}
			}
			else if (StockAbilities.isChiBlocking(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Chi." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					chiabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Chi." + a.name() + ".Description"));
					if (a == StockAbilities.HighJump) harmlessabilities.add(a.name());
				}
			}
			else {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities." + a.name() + ".Enabled")) {
					abilities.add(a.name()); // AvatarState, etc.
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities." + a.name() + ".Description"));
				}
			}
		}
		for (AbilityModule ab: ability) {
			try {
				//To check if EarthBlast == Earthblast or for example, EarthBlast == EARTHBLAST
				boolean succes = true;
				for(String enabledAbility : abilities){
					if(enabledAbility.equalsIgnoreCase(ab.getName())){
						succes = false;
					}
				}
				if (!succes)
					continue;
				ab.onThisLoad();
				abilities.add(ab.getName());
				for (StockAbilities a: StockAbilities.values()) {
					if (a.name().equalsIgnoreCase(ab.getName())){
						disabledStockAbilities.add(a.name());
					}
				}
				if (ab.getElement() == Element.Air.toString()) airbendingabilities.add(ab.getName()); 
				if (ab.getElement() == Element.Water.toString()) waterbendingabilities.add(ab.getName());
				if (ab.getElement() == Element.Earth.toString()) earthbendingabilities.add(ab.getName());
				if (ab.getElement() == Element.Fire.toString()) firebendingabilities.add(ab.getName());
				if (ab.getElement() == Element.Chi.toString()) chiabilities.add(ab.getName());
				if (ab.isShiftAbility()) shiftabilities.add(ab.getName());
				if (ab.isHarmlessAbility()) harmlessabilities.add(ab.getName());
				
				if (ab.getSubElement() != null)
				{
					subabilities.add(ab.getName());
					switch(ab.getSubElement())
					{
						case Bloodbending:
							bloodabilities.add(ab.getName());
							break;
						case Combustion:
							combustionabilities.add(ab.getName());
							break;
						case Flight:
							flightabilities.add(ab.getName());
							break;
						case Healing:
							healingabilities.add(ab.getName());
							break;
						case Icebending:
							iceabilities.add(ab.getName());
							break;
						case Lavabending:
							lavaabilities.add(ab.getName());
							break;
						case Lightning:
							lightningabilities.add(ab.getName());
							break;
						case Metalbending:
							metalabilities.add(ab.getName());
							break;
						case Plantbending:
							plantabilities.add(ab.getName());
							break;
						case Sandbending:
							sandabilities.add(ab.getName());
							break;
						case SpiritualProjection:
							spiritualprojectionabilities.add(ab.getName());
							break;
					}
				}
				
				// if (ab.isMetalbendingAbility()) metalbendingabilities.add(ab.getName());
				descriptions.put(ab.getName(), ab.getDescription());
				authors.put(ab.getName(), ab.getAuthor());
			} catch (AbstractMethodError e) { //If triggered means ability was made pre 1.6 BETA 8
				ProjectKorra.log.warning("The ability " + ab.getName() + " is either broken or outdated. Please remove it!");
				//e.printStackTrace();
				ab.stop();
				abilities.remove(ab.getName());
				final AbilityModule skill = ab;
				//Bellow to avoid ConcurrentModificationException
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						ability.remove(skill);
					}
				}, 10);
				continue;
			}
		}
		
		Collections.sort(airbendingabilities);
		Collections.sort(waterbendingabilities);
		Collections.sort(earthbendingabilities);
		Collections.sort(firebendingabilities);
		Collections.sort(chiabilities);
	}

}
