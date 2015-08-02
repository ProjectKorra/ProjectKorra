package com.projectkorra.ProjectKorra.Ability.MultiAbility;

import java.io.File;
import java.util.List;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.MultiAbilityManager;
import com.projectkorra.ProjectKorra.MultiAbilityManager.MultiAbility;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.Utilities.AbilityLoader;

public class MultiAbilityModuleManager {
	private final AbilityLoader<MultiAbilityModule> loader;
	public static List<MultiAbilityModule> multiAbility;

	public MultiAbilityModuleManager() {
		final File path = new File(ProjectKorra.plugin.getDataFolder().toString() + "/MultiAbilities/");
		if (!path.exists()) {
			path.mkdir();
		}

		loader = new AbilityLoader<MultiAbilityModule>(ProjectKorra.plugin, path, new Object[] {});
		multiAbility = loader.load(MultiAbilityModule.class);

		loadMAModules();
	}

	private void loadMAModules() {
		for (MultiAbilityModule mam : multiAbility) {
			mam.onThisLoad();
			AbilityModuleManager.abilities.add(mam.getName());
			for (StockAbilities a : StockAbilities.values()) {
				if (a.name().equalsIgnoreCase(mam.getName())) {
					AbilityModuleManager.disabledStockAbilities.add(a.name());
				}
			}
			if (mam.getElement() == Element.Air.toString()) AbilityModuleManager.airbendingabilities.add(mam.getName());
			if (mam.getElement() == Element.Water.toString()) AbilityModuleManager.waterbendingabilities.add(mam.getName());
			if (mam.getElement() == Element.Earth.toString()) AbilityModuleManager.earthbendingabilities.add(mam.getName());
			if (mam.getElement() == Element.Fire.toString()) AbilityModuleManager.firebendingabilities.add(mam.getName());
			if (mam.getElement() == Element.Chi.toString()) AbilityModuleManager.chiabilities.add(mam.getName());
			AbilityModuleManager.shiftabilities.add(mam.getName());
			if (mam.isHarmlessAbility()) AbilityModuleManager.harmlessabilities.add(mam.getName());

			if (mam.getSubElement() != null) {
				AbilityModuleManager.subabilities.add(mam.getName());
				switch (mam.getSubElement()) {
					case Bloodbending:
						AbilityModuleManager.bloodabilities.add(mam.getName());
						break;
					case Combustion:
						AbilityModuleManager.combustionabilities.add(mam.getName());
						break;
					case Flight:
						AbilityModuleManager.flightabilities.add(mam.getName());
						break;
					case Healing:
						AbilityModuleManager.healingabilities.add(mam.getName());
						break;
					case Icebending:
						AbilityModuleManager.iceabilities.add(mam.getName());
						break;
					case Lavabending:
						AbilityModuleManager.lavaabilities.add(mam.getName());
						break;
					case Lightning:
						AbilityModuleManager.lightningabilities.add(mam.getName());
						break;
					case Metalbending:
						AbilityModuleManager.metalabilities.add(mam.getName());
						break;
					case Plantbending:
						AbilityModuleManager.plantabilities.add(mam.getName());
						break;
					case Sandbending:
						AbilityModuleManager.sandabilities.add(mam.getName());
						break;
					case SpiritualProjection:
						AbilityModuleManager.spiritualprojectionabilities.add(mam.getName());
						break;
				}
			}

			MultiAbilityManager.multiAbilityList.add(new MultiAbility(mam.getName(), mam.getAbilities()));
			AbilityModuleManager.descriptions.put(mam.getName(), mam.getDescription());
			AbilityModuleManager.authors.put(mam.getName(), mam.getAuthor());
		}
	}
}