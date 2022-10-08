package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.util.DBCooldownManager;
import com.projectkorra.projectkorra.util.FlightHandler;
import com.projectkorra.projectkorra.util.StatisticsManager;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class Manager implements Listener {

	/**
	 * {@link Map} containing all {@link Manager} instances by their
	 * {@link Class} as key
	 */
	private static final Map<Class<? extends Manager>, Manager> MANAGERS = new HashMap<>();

	/**
	 * Register a new {@link Manager} instance.
	 *
	 * @param managerClass {@link Class} of the {@link Manager} to be registered
	 * @throws NullPointerException if managerClass is null
	 * @throws IllegalArgumentException if managerClass has already been
	 *             registered
	 */
	public static void registerManager(final Class<? extends Manager> managerClass) {
		Validate.notNull(managerClass, "Manager class cannot be null");
		Validate.isTrue(!MANAGERS.containsKey(managerClass), "Manager has already been registered");
		try {
			final Constructor<? extends Manager> constructor = managerClass.getDeclaredConstructor();
			final boolean accessible = constructor.isAccessible();
			constructor.setAccessible(true);
			final Manager manager = constructor.newInstance();
			constructor.setAccessible(accessible);
			manager.activate();
			MANAGERS.put(managerClass, manager);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get a registered {@link Manager} by its {@link Class}.
	 *
	 * @param managerClass {@link Class} of the registered {@link Manager}
	 * @return instance of the {@link Manager} class
	 * @throws NullPointerException if managerClass is null
	 * @throws IllegalArgumentException if managerClass has not yet been
	 *             registered
	 */
	public static <T extends Manager> T getManager(final Class<T> managerClass) {
		Validate.notNull(managerClass, "Manager class cannot be null");
		Validate.isTrue(MANAGERS.containsKey(managerClass), "Manager has not yet been registered");
		final Manager registered = MANAGERS.get(managerClass);
		return managerClass.cast(registered);
	}

	/**
	 * Get this plugin instance
	 *
	 * @return {@link ProjectKorra} plugin instance
	 */
	protected ProjectKorra getPlugin() {
		return JavaPlugin.getPlugin(ProjectKorra.class);
	}

	/**
	 * Activate this {@link Manager}
	 */
	public final void activate() {
		Bukkit.getPluginManager().registerEvents(this, ProjectKorra.plugin);
		this.onActivate();
	}

	/**
	 * Overridable method to execute code when this {@link Manager} is activated
	 */
	public void onActivate() {

	}

	/**
	 * Deactivate this {@link Manager}
	 */
	public final void deactivate() {
		HandlerList.unregisterAll(this);
		this.onDeactivate();
	}

	/**
	 * Overridable method to execute code when this {@link Manager} is
	 * deactivated
	 */
	public void onDeactivate() {

	}

	/**
	 * Activates core {@link Manager} instances
	 */
	public static void startup() {
		registerManager(StatisticsManager.class);
		registerManager(DBCooldownManager.class);
		registerManager(FlightHandler.class);
	}

	/**
	 * Deactivates and clears all {@link Manager} instances
	 */
	public static void shutdown() {
		MANAGERS.values().forEach(Manager::deactivate);
		MANAGERS.clear();
	}
}
