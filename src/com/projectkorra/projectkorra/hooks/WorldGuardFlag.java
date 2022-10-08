package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.ProjectKorra;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardFlag {
	public static void registerBendingWorldGuardFlag() {
		final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

		try {
			registry.register(new StateFlag("bending", false));
		} catch (final Exception e) {
			ProjectKorra.log.severe("Unable to register bending WorldGuard flag: " + e);
		}
	}
}
