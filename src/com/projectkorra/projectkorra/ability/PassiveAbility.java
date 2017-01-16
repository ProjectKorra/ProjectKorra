package com.projectkorra.projectkorra.ability;

public interface PassiveAbility {

	/**
	 * 
	 * @return false if the passive does not actually do anything, but so it
	 *         still registers in /b d [element]passive
	 */
	public boolean isInstantiable();

}
