package com.projectkorra.projectkorra.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.jar.JarFile;

/**
 * Called when an ability is successfully loaded.
 */
public class AbilityLoadEvent<T> extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Plugin plugin;
	private final T loadable;
	private final JarFile jarFile;

	/**
	 * Creates a new AbilityLoadEvent.
	 * 
	 * @param plugin The instance of ProjectKorra
	 * @param loadable The class that was loaded
	 * @param jarFile The JarFile the class was loaded from
	 */
	public AbilityLoadEvent(Plugin plugin, T loadable, JarFile jarFile) {
		this.plugin = plugin;
		this.loadable = loadable;
		this.jarFile = jarFile;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Gets the JarFile the ability was loaded from.
	 * 
	 * @return The JarFile from the event
	 */
	public JarFile getJarFile() {
		return jarFile;
	}

	/**
	 * Gets the ability's class that was loaded.
	 * 
	 * @return The loaded class
	 */
	public T getLoadable() {
		return loadable;
	}

	/**
	 * Gets the ProjectKorra instance the ability was loaded into.
	 * 
	 * @return The ProjectKorra instance
	 */
	public Plugin getPlugin() {
		return plugin;
	}
}
