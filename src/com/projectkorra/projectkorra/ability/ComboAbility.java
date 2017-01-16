package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface ComboAbility {

	/**
	 * Accessor Method to get the instructions for using this combo.
	 *
	 * @return The steps for the combo.
	 */

	public abstract Object createNewComboInstance(Player player);

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	public abstract ArrayList<AbilityInformation> getCombination();

}
