package com.projectkorra.projectkorra.ability;

public interface SubAbility {

	/**
	 * Specifies the Class of this Ability's parent. For example, a
	 * LightningAbility would return FireAbility.class.
	 * 
	 * @return the Class of the parent
	 */
	public Class<? extends Ability> getParentAbility();

}
