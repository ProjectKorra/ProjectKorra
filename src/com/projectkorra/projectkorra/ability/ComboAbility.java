package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public interface ComboAbility {

	/**
	 * Creates a new instance of this combo type with default config
	 */
	public abstract Object createNewComboInstance(Player player);

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	public abstract ArrayList<AbilityInformation> getCombination();
}
