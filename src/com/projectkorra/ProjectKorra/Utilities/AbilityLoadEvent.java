package com.projectkorra.ProjectKorra.Utilities;

import java.util.jar.JarFile;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class AbilityLoadEvent<T> extends Event{

	private static final HandlerList handlers = new HandlerList();
	
	private final Plugin plugin;
	private final T loadable;
	private final JarFile jarFile;
	
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
	
	public JarFile getJarFile() {
		return jarFile;
	}
	
	public T getLoadable() {
		return loadable;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
}
