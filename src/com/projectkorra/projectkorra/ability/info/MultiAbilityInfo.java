package com.projectkorra.projectkorra.ability.info;

import com.projectkorra.projectkorra.ability.Ability;

import java.util.List;

public interface MultiAbilityInfo extends AbilityInfo {

	/**
	 * Returns the sub abilities of a MultiAbility. For example:
	 * <p>
	 * {@code new
	 * MultiAbilitySub("SubAbility", Element.LIGHTNING);}
	 *
	 * @return a list of sub MultiAbilities
	 */
	List<Class<? extends Ability>> getAbilities();
}
