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

}
