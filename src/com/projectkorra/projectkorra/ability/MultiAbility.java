package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;

import java.util.ArrayList;

public interface MultiAbility {
	
	/**
	 * Returns the sub abilities of a MultiAbility. For example: <p>{@code new
	 * MultiAbilitySub("SubAbility", Element.LIGHTNING);}
	 * 
	 * @return a list of sub MultiAbilities
	 */
	public abstract ArrayList<MultiAbilityInfoSub> getMultiAbilities();
	
}
