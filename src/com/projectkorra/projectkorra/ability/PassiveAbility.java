package com.projectkorra.projectkorra.ability;

public interface PassiveAbility {

	/**
	 * This is a check to see if the passive requires some form of activation,
	 * such as sneaking, clicking, etc. <br>
	 * <b>If false, the passive should not call start!</b>
	 *
	 * @return false if this passive should always be on
	 */
	boolean isInstantiable();

	/**
	 * This is used if the passive should progress
	 *
	 * @return false if progress() shouldn't be called;
	 */
	boolean isProgressable();

}
