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
import org.bukkit.Bukkit;


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
	
	public static HashMap<String, String> descriptions;

	public AbilityModuleManager(final ProjectKorra plugin) {
		AbilityModuleManager.plugin = plugin;
		final File path = new File(plugin.getDataFolder().toString() + "/Abilities/");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new AbilityLoader<AbilityModule>(plugin, path, new Object[] {});
		abilities = new HashSet<String>();
                disabledStockAbilities = new HashSet<String>();
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
		ability = loader.load(AbilityModule.class);
		disabledStockAbilities = new HashSet<String>();
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
					if (a == StockAbilities.IceSpike) shiftabilities.add(a.name());
					if (a == StockAbilities.IceBlast) shiftabilities.add(a.name());
					if (a == StockAbilities.WaterWave) shiftabilities.add(a.name());
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
					if (a == StockAbilities.Extraction) metalbendingabilities.add(a.name());
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
			//To check if EarthBlast == Earthblast or for example, EarthBlast == EARTHBLAST
                    boolean abilityIsOn= false;
                        for(String enabledAbility : abilities){
                            if(enabledAbility.equalsIgnoreCase(ab.getName())){
                                abilityIsOn = true;
                            }
                        }
                        if (abilityIsOn)
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
			if (ab.isMetalbendingAbility()) metalbendingabilities.add(ab.getName());
			descriptions.put(ab.getName(), ab.getDescription());
			authors.put(ab.getName(), ab.getAuthor());
		}
		Collections.sort(airbendingabilities);
		Collections.sort(waterbendingabilities);
		Collections.sort(earthbendingabilities);
		Collections.sort(firebendingabilities);
		Collections.sort(chiabilities);
	}

}
