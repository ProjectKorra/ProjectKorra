package com.projectkorra.projectkorra.ability.info;

import com.projectkorra.projectkorra.ability.AbilityManager;

public interface AddonAbilityInfo extends AbilityInfo {

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
}
