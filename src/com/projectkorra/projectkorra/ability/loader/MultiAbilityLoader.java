package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.MultiAbilityManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class MultiAbilityLoader extends AbilityLoader {

	/**
	 * Returns the sub abilities of a MultiAbility. For example:
	 * <p>
	 * {@code new
	 * MultiAbilitySub("SubAbility", Element.LIGHTNING);}
	 *
	 * @return a list of sub MultiAbilities
	 */
	public abstract List<Class<? extends Ability>> getAbilities();
}
