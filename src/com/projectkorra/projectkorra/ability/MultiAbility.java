package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;

import java.util.ArrayList;

public interface MultiAbility {
	
	/**
	 * Returns the sub abilities of a MultiAbility. e.g. {@code new
	 * MultiAbilitySub("SubAbility", Element.Fire, SubElement.Lightning);}
	 * 
	 * @return arraylist of multiabilitysub
	 */
	public abstract ArrayList<MultiAbilityInfoSub> getMultiAbilities();
	
}
