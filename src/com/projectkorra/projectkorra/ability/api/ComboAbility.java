package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.ComboAbilityManager;

import java.util.LinkedList;

public interface ComboAbility {

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return A {@link LinkedList} containing the combo's steps.
	 */
	LinkedList<ComboAbilityManager.Combination> getCombination();
}
