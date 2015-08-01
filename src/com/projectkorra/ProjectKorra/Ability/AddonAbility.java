package com.projectkorra.ProjectKorra.Ability;

/**
 * Represents a {@link CoreAbility} that is either an addon or
 * an addition to an existing ability 
 */
public abstract class AddonAbility extends CoreAbility {
	
	@Override
	public StockAbility getStockAbility() {
		return null;
	}

}
