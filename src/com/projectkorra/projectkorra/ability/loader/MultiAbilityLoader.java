package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;

import java.util.ArrayList;

public abstract class MultiAbilityLoader extends AbilityLoader {

	/**
	 * Returns the sub abilities of a MultiAbility. For example:
	 * <p>
	 * {@code new
	 * MultiAbilitySub("SubAbility", Element.LIGHTNING);}
	 *
	 * @return a list of sub MultiAbilities
	 */
	public abstract ArrayList<MultiAbilityManager.MultiAbilityInfoSub> getMultiAbilities();
}
