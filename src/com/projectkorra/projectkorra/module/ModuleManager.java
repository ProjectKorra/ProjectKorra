package com.projectkorra.projectkorra.module;

import com.google.common.base.Preconditions;
import com.projectkorra.projectkorra.ability.AbilityManager;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.cooldown.CooldownManager;
import com.projectkorra.projectkorra.database.DatabaseManager;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.player.BendingPlayerManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

	private static final Map<Class<? extends Module>, Module> MODULES = new HashMap<>();

	/**
	 * Registers a new {@link Module} instance.
	 *
	 * @param moduleClass {@link Class} of the {@link Module} to be registered
	 * @throws NullPointerException     if moduleClass is null
	 * @throws IllegalArgumentException if moduleClass has already been registered
	 */
	public static <T extends Module> T registerModule(Class<T> moduleClass) {
		Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null");
		Preconditions.checkArgument(!MODULES.containsKey(moduleClass), "moduleClass has already been registered");

		try {
			Constructor<T> constructor = moduleClass.getDeclaredConstructor();
			boolean accessible = constructor.isAccessible();
			constructor.setAccessible(true);

			T module = constructor.newInstance();

			MODULES.put(moduleClass, module);
			module.enable();

			constructor.setAccessible(accessible);
			return module;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a registered {@link Module} by its {@link Class}.
	 *
	 * @param moduleClass {@link Class} of the registered {@link Module}
	 * @return instance of the {@link Module} class
	 * @throws NullPointerException     if moduleClass is null
	 * @throws IllegalArgumentException if moduleClass has not been registered
	 */
	public static <T extends Module> T getModule(Class<T> moduleClass) {
		Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null");

		if (!MODULES.containsKey(moduleClass)) {
			return registerModule(moduleClass);
		}

		return moduleClass.cast(MODULES.get(moduleClass));
	}

	/**
	 * Register all our core {@link Module}s onEnable.
	 */
	public static void startup() {
		MODULES.values().forEach(Module::disable);
		MODULES.clear();

		registerModule(DatabaseManager.class);
		registerModule(BendingPlayerManager.class);
		registerModule(ElementManager.class);
		registerModule(AbilityManager.class);
		registerModule(AbilityBindManager.class);
		registerModule(CooldownManager.class);
	}

	/**
	 * Disable all our core {@link Module}s onDisable.
	 */
//	public static void shutdown() {
//		registerModule(CooldownManager.class);
//		registerModule(AbilityBindManager.class);
//		registerModule(AbilityManager.class);
//		registerModule(ElementManager.class);
//		getModule(BendingPlayerManager.class).disable();
//		getModule(DatabaseManager.class).disable();
//	}
}
