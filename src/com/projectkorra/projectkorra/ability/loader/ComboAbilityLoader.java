package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.util.ComboManager;

import java.util.ArrayList;

public abstract class ComboAbilityLoader extends AbilityLoader {

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	public abstract ArrayList<ComboManager.AbilityInformation> getCombination();
}
