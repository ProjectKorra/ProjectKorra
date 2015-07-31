package com.projectkorra.ProjectKorra.Ability;

/**
 * Represents a {@link Ability} that is either an addon or
 * an addition to an existing ability 
 */
public abstract class AddonAbility extends CoreAbility {
	
	@Override
	public StockAbilities getStockAbility() {
		return null;
	}

}
