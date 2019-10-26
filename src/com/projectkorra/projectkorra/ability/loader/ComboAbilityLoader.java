package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.ComboAbilityManager;

import java.util.LinkedList;

public abstract class ComboAbilityLoader extends AbilityLoader {

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return A {@link LinkedList} containing the combo's steps.
	 */
	public abstract LinkedList<ComboAbilityManager.Combination> getCombination();
}
