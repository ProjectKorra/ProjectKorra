package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AbilityRegistry<T> {

	private final Plugin plugin;
	private ClassLoader loader;
	private JarFile jar;
	private String path;

	public AbilityRegistry(final JavaPlugin plugin, final String packageBase) {
		this.plugin = plugin;
		this.loader = plugin.getClass().getClassLoader();
		this.path = packageBase.replace('.', '/');

		if (plugin == null || this.loader == null) {
			ProjectKorra.log.severe("Could not find classloader! Will not load abilities from " + packageBase);
			return;
		}

		try {
			final Enumeration<URL> resources = this.loader.getResources(this.path);

			String jarloc = resources.nextElement().getPath();
			jarloc = jarloc.substring(5, jarloc.length() - this.path.length() - 2);

			final String s = URLDecoder.decode(jarloc, "UTF-8");

			this.jar = new JarFile(new File(s));

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a list of loaded objects of the provided classType.
	 *
	 * @param classType Type of class to load
	 * @param parentClass Type of class that the class must extend. Use
	 *            {@code Object.class} for classes without a type.
	 * @return
	 */
	public List<Class<T>> load(final Class<?> classType, final Class<?> parentClass) {
		final ArrayList<Class<T>> loadables = new ArrayList<>();

		if (this.loader == null || this.jar == null) {
			return loadables;
		}

		final Enumeration<JarEntry> entries = this.jar.entries();

		while (entries.hasMoreElements()) {

			final JarEntry entry = entries.nextElement();
			if (!entry.getName().endsWith(".class") || entry.getName().contains("$")) {
				continue;
			}

			final String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
			if (!className.startsWith(this.path.replace('/', '.'))) {
				continue;
			}

			Class<?> clazz = null;
			try {
				clazz = Class.forName(className, true, this.loader);

				if (!classType.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}

				Class<T> loadable = (Class<T>) clazz;

				loadables.add(loadable);

				final AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(this.plugin, loadable, this.jar);
				this.plugin.getServer().getPluginManager().callEvent(event);
			} catch (Exception | Error e) {
				continue;
			}
		}

		return loadables;
	}

}
