package com.projectkorra.ProjectKorra.Ability;

import java.io.File;
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
	public static HashSet<String> waterbendingabilities;
	public static HashSet<String> airbendingabilities;
	public static HashSet<String> earthbendingabilities;
	public static HashSet<String> firebendingabilities;
	public static HashSet<String> chiabilities;
	public static HashSet<String> shiftabilities;
	public static HashMap<String, String> authors;
	public static HashSet<String> harmlessabilities;
	public static HashSet<String> igniteabilities;
	public static HashSet<String> explodeabilities;
	
	public static HashMap<String, String> descriptions;

	public AbilityModuleManager(final ProjectKorra plugin) {
		AbilityModuleManager.plugin = plugin;
		final File path = new File(plugin.getDataFolder().toString() + "/Abilities/");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new AbilityLoader<AbilityModule>(plugin, path, new Object[] {});
		abilities = new HashSet<String>();
		waterbendingabilities = new HashSet<String>();
		airbendingabilities = new HashSet<String>();
		earthbendingabilities = new HashSet<String>();
		firebendingabilities = new HashSet<String>();
		chiabilities = new HashSet<String>();
		shiftabilities = new HashSet<String>();
		descriptions = new HashMap<String, String>();
		authors = new HashMap<String, String>();
		harmlessabilities = new HashSet<String>();
		explodeabilities = new HashSet<String>();
		igniteabilities = new HashSet<String>();
		ability = loader.load(AbilityModule.class);
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
				}
			}
			else if (StockAbilities.isWaterbending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					waterbendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Water." + a.name() + ".Description"));
					if (a == StockAbilities.WaterSpout) harmlessabilities.add(a.name());
					if (a == StockAbilities.HealingWaters) harmlessabilities.add(a.name());
				}
			}
			else if (StockAbilities.isEarthbending(a)) {
				if (ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth." + a.name() + ".Enabled")) {
					abilities.add(a.name());
					earthbendingabilities.add(a.name());
					descriptions.put(a.name(), ProjectKorra.plugin.getConfig().getString("Abilities.Earth." + a.name() + ".Description"));
					if (a == StockAbilities.Tremorsense) harmlessabilities.add(a.name());
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
			if (abilities.contains(ab.getName())) {
				continue;
			}
			ab.onThisLoad();
			abilities.add(ab.getName());
			if (ab.getElement() == Element.Air.toString()) airbendingabilities.add(ab.getName()); 
			if (ab.getElement() == Element.Water.toString()) waterbendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Earth.toString()) earthbendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Fire.toString()) firebendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Chi.toString()) chiabilities.add(ab.getName());
			if (ab.isShiftAbility()) shiftabilities.add(ab.getName());
			if (ab.isHarmlessAbility()) harmlessabilities.add(ab.getName());
			descriptions.put(ab.getName(), ab.getDescription());
			authors.put(ab.getName(), ab.getAuthor());
		}
	}

}
