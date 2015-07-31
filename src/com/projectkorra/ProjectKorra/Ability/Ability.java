package com.projectkorra.ProjectKorra.Ability;

import com.projectkorra.ProjectKorra.configuration.ConfigLoadable;

public interface Ability extends ConfigLoadable {
	
	/**
	 * A method to tell an Ability to start.
	 */
	public boolean progress();
	
	/**
	 * A method to remove an instance of an Ability.
	 */
	public void remove();
	
	/**
	 * Gets the {@link InstanceType} of the ability.
	 * 
	 * @return the instance type
	 */
	public InstanceType getInstanceType();
	
	/**
	 * Used to signify whether an ability can have multiple instances
	 * per player or only a single instance.
	 */
	public enum InstanceType {
		SINGLE, MULTIPLE;
	}

}
