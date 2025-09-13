package com.projectkorra.projectkorra.ability;

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
	 * they will be cleaned up by {@link CoreAbility#removeAll()}.
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

	/**
	 * @return If the ability can be used by players by default. If true, a
	 * permission node will be created and granted to default players
	 */
	default boolean isDefault() {
		return true;
	}
}
