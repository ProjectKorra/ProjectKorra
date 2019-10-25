package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.ComboManager;

import java.util.LinkedList;

public abstract class ComboAbilityLoader extends AbilityLoader {

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return A {@link LinkedList} containing the combo's steps.
	 */
	public abstract LinkedList<ComboManager.Combination> getCombination();
}
