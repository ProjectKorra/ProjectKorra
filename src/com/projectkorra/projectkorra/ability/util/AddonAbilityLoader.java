package com.projectkorra.projectkorra.ability.util;

import sun.reflect.ReflectionFactory;

import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.util.FileExtensionFilter;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

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

public class AddonAbilityLoader<T> {

	private final Plugin plugin;
	private final File directory;
	private final ArrayList<File> files;
	private ClassLoader loader;

	public AddonAbilityLoader(Plugin plugin, File directory) {
		this.plugin = plugin;
		this.directory = directory;
		this.files = new ArrayList<File>();

		if (plugin == null || directory == null) {
			return;
		}

		for (File f : directory.listFiles(new FileExtensionFilter(".jar"))) {
			files.add(f);
		}

		List<URL> urls = new ArrayList<URL>();
		for (File file : files) {
			try {
				urls.add(file.toURI().toURL());
			}
			catch (MalformedURLException e) {
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
	@SuppressWarnings("unchecked")
	public List<T> load(Class<?> classType, Class<?> parentClass) {
		ArrayList<T> loadables = new ArrayList<>();

		for (File file : files) {
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(file);
				Enumeration<JarEntry> entries = jarFile.entries();

				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class")) {
						continue;
					}

					String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
					Class<?> clazz = null;
					try {
						clazz = Class.forName(className, true, loader);
					}
					catch (Exception | Error e) {
						continue;
					}

					if (!classType.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
						continue;
					}

					ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
					Constructor<?> objDef = parentClass.getDeclaredConstructor();
					Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
					T loadable = (T) clazz.cast(intConstr.newInstance());

					loadables.add(loadable);
					AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(plugin, loadable, jarFile);
					plugin.getServer().getPluginManager().callEvent(event);
				}

			}
			catch (Exception | Error e) {
				e.printStackTrace();
				plugin.getLogger().log(Level.WARNING, "Unknown cause");
				plugin.getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			}
			finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return loadables;
	}

	public ClassLoader getLoader() {
		return loader;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public File getDirectory() {
		return directory;
	}

	public ArrayList<File> getFiles() {
		return files;
	}

}
