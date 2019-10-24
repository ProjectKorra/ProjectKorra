package com.projectkorra.projectkorra.ability.loader;

public abstract class PassiveAbilityLoader extends AbilityLoader {

	/**
	 * This is a check to see if the passive requires some form of activation,
	 * such as sneaking, clicking, etc. <br>
	 * <b>If false, the passive should not call start!</b>
	 *
	 * @return false if this passive should always be on
	 */
	public abstract boolean isInstantiable();

	/**
	 * This is used if the passive should progress
	 *
	 * @return false if progress() shouldn't be called;
	 */
	public abstract boolean isProgressable();

}
