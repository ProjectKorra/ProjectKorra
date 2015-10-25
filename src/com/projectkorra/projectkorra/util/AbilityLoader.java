package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import com.projectkorra.projectkorra.util.AbilityLoadable.LoadResult;
import com.projectkorra.projectkorra.util.AbilityLoadable.LoadResult.Result;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AbilityLoader<T extends AbilityLoadable> implements Listener {

	private final Plugin plugin;

	private final File dir;
	private ClassLoader loader;
	private final Object[] paramTypes;
	private final Class<?>[] ctorParams;

	private final ArrayList<File> files;
	private final List<T> loadables;

	public AbilityLoader(Plugin plugin, File dir, Object... paramTypes) {
		this.plugin = plugin;
		this.dir = dir;
		this.paramTypes = paramTypes;
		this.files = new ArrayList<File>();
		this.loadables = new ArrayList<T>(0);

		for (File f : dir.listFiles(new FileExtensionFilter(".jar"))) {
			files.add(f);
		}

		List<Class<?>> constructorParams = new ArrayList<Class<?>>();

		for (Object paramType : paramTypes)
			constructorParams.add(paramType.getClass());

		this.ctorParams = constructorParams.toArray(new Class<?>[0]);

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

	public Logger getLogger() {
		return plugin.getLogger();
	}

	@SuppressWarnings("unchecked")
	public final List<T> load(Class<? extends AbilityLoadable> classType) {
		for (File file : files) {
			try (final JarFile jarFile = new JarFile(file)) {
				String mainClass = null;

				if (jarFile.getEntry("path.yml") != null) {
					JarEntry element = jarFile.getJarEntry("path.yml");
					BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
					mainClass = reader.readLine().substring(12);
				}

				if (mainClass != null) {
					Class<?> clazz = Class.forName(mainClass, true, loader);

					if (clazz != null) {
						Class<? extends AbilityLoadable> loadableClass = clazz.asSubclass(classType);
						Constructor<? extends AbilityLoadable> ctor = loadableClass.getConstructor(ctorParams);
						T loadable = (T) ctor.newInstance(paramTypes);

						LoadResult result = loadable.init();

						if (result.getResult().equals(Result.SUCCESS)) {
							loadables.add(loadable);
							AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(plugin, loadable, jarFile);
							plugin.getServer().getPluginManager().callEvent(event);
							continue;
						}

						String reason = result.getReason();

						if (reason != null && !reason.isEmpty()) {
							getLogger().log(Level.INFO, "The JAR file " + file.getName() + " was unable to load because: " + reason);
						}
					} else {
						jarFile.close();
						throw new ClassNotFoundException();
					}
				} else {
					jarFile.close();
					throw new ClassNotFoundException();
				}
			}
			catch (ClassCastException e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "The JAR file " + file.getPath() + " is in the wrong directory");
				getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "Invalid path.yml");
				getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load.");
			}
			catch (Exception e) {
				e.printStackTrace();
				getLogger().log(Level.WARNING, "Unknown cause");
				getLogger().log(Level.WARNING, "The JAR file " + file.getName() + " failed to load");
			}
		}

		return loadables;
	}

	public List<T> reload(Class<? extends AbilityLoadable> classType) {
		unload();

		List<URL> urls = new ArrayList<URL>();
		files.clear();
		for (String loadableFile : dir.list()) {
			if (loadableFile.endsWith(".jar")) {
				File file = new File(dir, loadableFile);
				files.add(file);
				try {
					urls.add(file.toURI().toURL());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		this.loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), plugin.getClass().getClassLoader());

		return load(classType);
	}

	public void unload() {
		loadables.clear();
	}
}
