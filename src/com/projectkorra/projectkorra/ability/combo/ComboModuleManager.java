package com.projectkorra.projectkorra.ability.combo;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.AbilityLoader;

import java.io.File;
import java.util.List;

public class ComboModuleManager {
	private final AbilityLoader<ComboAbilityModule> loader;
	public static List<ComboAbilityModule> combo;

	public ComboModuleManager() {
		final File path = new File(ProjectKorra.plugin.getDataFolder().toString() + "/Combos/");
		if (!path.exists()) {
			path.mkdir();
		}

		loader = new AbilityLoader<ComboAbilityModule>(ProjectKorra.plugin, path, new Object[] {});

		combo = loader.load(ComboAbilityModule.class);

		loadComboModules();
	}

	private void loadComboModules() {
		for (ComboAbilityModule cm : combo) {
			cm.onThisLoad();
			ComboManager.comboAbilityList.add(new ComboManager.ComboAbility(cm.getName(), cm.getCombination(), cm));
			ComboManager.descriptions.put(cm.getName(), cm.getDescription());
			ComboManager.instructions.put(cm.getName(), cm.getInstructions());
			ComboManager.authors.put(cm.getName(), cm.getAuthor());
		}
	}
}
