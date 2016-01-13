package com.projectkorra.projectkorra.ability;

public interface AddonAbility {
	
	/**
	 * Called when the ability is loaded by PK. This is where the developer
	 * registers Listeners and Permissions.
	 */
	public void load();

	/**
	 * Void Method called whenever ProjectKorra stops and the ability is
	 * unloaded.
	 */
	public void stop();
	
	public String getAuthor();
	
	/**
	 * Accessor Method to get the version of the ability.
	 * 
	 * @return The version of the ability as a String.
	 */
	public String getVersion();
}
