package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.AbilityManager;

public interface AddonAbility {

	/**
	 * Called when the ability is loaded by PK. This is where the developer
	 * registers Listeners and Permissions.
	 */
	void load();

	/**
	 * Called whenever ProjectKorra stops and the ability is unloaded. This
	 * method is useful for cleaning up leftover objects such as frozen blocks.
	 * Any CoreAbility instances do not need to be cleaned up by stop method, as
	 * they will be cleaned up by {@link AbilityManager#removeAll()}.
	 */
	void stop();

	/**
	 * @return the name of the author of this AddonAbility
	 */
	String getAuthor();

	/**
	 * @return The version of the ability as a String.
	 */
	String getVersion();
}
