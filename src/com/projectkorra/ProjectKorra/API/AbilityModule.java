package com.projectkorra.ProjectKorra.API;

import com.projectkorra.ProjectKorra.Utilities.AbilityLoadable;

public abstract class AbilityModule extends AbilityLoadable implements Cloneable {

	/**
	 * Name of the Ability which will be used in commands.
	 * @param name
	 */
	public AbilityModule(final String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default action on loading this ability.
	 */
	abstract public void onThisLoad();
	
	/**
	 * The version of the ability. Must be overridden
	 * @return {@code String} Version of ability
	 */
	public String version() {
		return "outdated";
	}
	
	/**
	 * Gets the Element name of this ability. 
	 * 
	 * <p>
	 * <strong>Example:</strong> {@code return} Element.Air.toString();
	 * </p>
	 * @return {@code String} Element name
	 */
	abstract public String getElement();
	
	/**
	 * Gets the author of this ability.
	 * @return {@code String} Name of author
	 */
	abstract public String getAuthor();
	
	/**
	 * Gets the description of the ability.
	 * @return {@code String} Ability description
	 */
	abstract public String getDescription();
	
	/**
	 * Action to take when ability stops.
	 */
	abstract public void stop();
	
	/**
	 * Checks if ability is a Shift ability.
	 * @return false By default
	 */
	public boolean isShiftAbility() {
		return false;
	}
	
	/**
	 * Checks if ability is a Harmless ability.
	 * @return true By default
	 */
	public boolean isHarmlessAbility() {
		return true;
	}
	
	/**
	 * Checks if ability is an Ignite ability.
	 * @return false By default
	 */
	public boolean isIgniteAbility() {
		return false;
	}
	
	/**
	 * Checks if ability is an Explode ability.
	 * @return false By default
	 */
	public boolean isExplodeAbility() {
		return false;
	}
	
	/**
	 * Checks if ability is a Metalbending ability.
	 * @return false By default
	 */
	public boolean isMetalbendingAbility() {
		return false;
	}
	

}
