package com.projectkorra.projectkorra.ability.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.util.FileExtensionFilter;

import sun.reflect.ReflectionFactory;

public class AddonAbilityLoader<T> {

	private final Plugin plugin;
	private final File directory;
	private final ArrayList<File> files;
	private ClassLoader loader;

	public AddonAbilityLoader(final Plugin plugin, final File directory) {
		this.plugin = plugin;
		this.directory = directory;
		this.files = new ArrayList<File>();

		if (plugin == null || directory == null) {
			return;
		}

		for (final File f : directory.listFiles(new FileExtensionFilter(".jar"))) {
			this.files.add(f);
		}

		final List<URL> urls = new ArrayList<URL>();
		for (final File file : this.files) {
			try {
				urls.add(file.toURI().toURL());
			} catch (final MalformedURLException e) {
				e.printStackTrace();
			}
		}
		this.loader = URLClassLoader.newInstance(urls.toArray(new URL[0]), plugin.getClass().getClassLoader());
	}

	/**
	 * @param classType
	 * @param parentClass a parent of classType that has a visible default
	 *            constructor
	 * @return A list of all of the T objects that were loaded from the jar
	 *         files within @param directory
	 */
	public List<T> load(final Class<?> classType, final Class<?> parentClass) {
		final ArrayList<T> loadables = new ArrayList<>();

		for (final File file : this.files) {
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(file);
				final Enumeration<JarEntry> entries = jarFile.entries();

				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class")) {
						continue;
					}

					final String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
					Class<?> clazz = null;
					try {
						clazz = Class.forName(className, true, this.loader);
					} catch (Exception | Error e) {
						continue;
					}

					if (!classType.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
						continue;
					}

					final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
					final Constructor<?> objDef = parentClass.getDeclaredConstructor();
					final Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
					final T loadable = (T) clazz.cast(intConstr.newInstance());

					loadables.add(loadable);
					final AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(this.plugin, loadable, jarFile);
					this.plugin.getServer().getPluginManager().callEvent(event);
				}

			} catch (Exception | Error e) {
				e.printStackTrace();
				this.plugin.getLogger().log(Level.WARNING, "Unknown cause");
				this.plugin.getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			} finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return loadables;
	}

	public ClassLoader getLoader() {
		return this.loader;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public File getDirectory() {
		return this.directory;
	}

	public ArrayList<File> getFiles() {
		return this.files;
	}

}
