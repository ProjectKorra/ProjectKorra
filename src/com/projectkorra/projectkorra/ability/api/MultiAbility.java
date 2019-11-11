package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.AbilityHandler;

import java.util.List;

public interface MultiAbility {

	/**
	 * Returns the sub abilities of a MultiAbility. For example:
	 * <p>
	 * {@code new
	 * MultiAbilitySub("SubAbility", Element.LIGHTNING);}
	 *
	 * @return a list of sub MultiAbilities
	 */
	List<AbilityHandler> getAbilities();
}
