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
	public abstract String getInstructions();

	/**
	 * Creates a new instance of the combo from a specific player.
	 * ProjectKorra's ComboModuleManager will use this method once the combo
	 * steps have been used by the player.
	 *
	 * @return A new instance of the ability.
	 * @param player The player using the combo.
	 */
	public abstract Object createNewComboInstance(Player player);

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	public abstract ArrayList<AbilityInformation> getCombination();
	
}
