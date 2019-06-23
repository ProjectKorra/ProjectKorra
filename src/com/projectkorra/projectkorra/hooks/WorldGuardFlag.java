package com.projectkorra.projectkorra.hooks;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import com.projectkorra.projectkorra.ProjectKorra;

public class WorldGuardFlag {
	public static void registerBendingWorldGuardFlag() {
		final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		try {
			registry.register(new StateFlag("bending", false));
		} catch (final FlagConflictException e) {
			ProjectKorra.log.severe("unable to register bending WorldGuard Flag");
		}
	}
}
