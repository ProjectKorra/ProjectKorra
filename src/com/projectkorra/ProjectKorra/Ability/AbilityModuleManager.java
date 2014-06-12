package com.projectkorra.ProjectKorra.Ability;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.ProjectKorra;

import Utilities.AbilityLoader;

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
		ability = loader.load(AbilityModule.class);
		fill();
	}
	
	private void fill() {
		for (AbilityModule ab: ability) {
			ab.onThisLoad();
			abilities.add(ab.getName());
			if (ab.getElement() == Element.Air.toString()) airbendingabilities.add(ab.getName()); 
			if (ab.getElement() == Element.Water.toString()) waterbendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Earth.toString()) earthbendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Fire.toString()) firebendingabilities.add(ab.getName());
			if (ab.getElement() == Element.Chi.toString()) chiabilities.add(ab.getName());
			if (ab.isShiftAbility()) shiftabilities.add(ab.getName());
			descriptions.put(ab.getName(), ab.getDescription());
			authors.put(ab.getName(), ab.getAuthor());
		}
	}

}
