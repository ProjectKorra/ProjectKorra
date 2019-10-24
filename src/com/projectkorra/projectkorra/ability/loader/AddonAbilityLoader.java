package com.projectkorra.projectkorra.ability.loader;

import com.projectkorra.projectkorra.ability.AbilityManager;

public abstract class AddonAbilityLoader extends AbilityLoader {

	/**
	 * Called when the ability is loaded by PK. This is where the developer
	 * registers Listeners and Permissions.
	 */
	public abstract void load();

	/**
	 * Called whenever ProjectKorra stops and the ability is unloaded. This
	 * method is useful for cleaning up leftover objects such as frozen blocks.
	 * Any CoreAbility instances do not need to be cleaned up by stop method, as
	 * they will be cleaned up by {@link AbilityManager#removeAll()}.
	 */
	public abstract void stop();
}
