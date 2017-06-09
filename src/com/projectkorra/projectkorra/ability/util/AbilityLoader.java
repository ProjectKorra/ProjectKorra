package com.projectkorra.projectkorra.ability.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.ProjectKorra;

import com.projectkorra.projectkorra.event.AbilityLoadEvent;

import sun.reflect.ReflectionFactory;

@SuppressWarnings("restriction")
public class AbilityLoader<T> {
	
	private final Plugin plugin;
	private ClassLoader loader;
	private JarFile jar;
	private String path;
	
	public AbilityLoader(JavaPlugin plugin, String packageBase) {
		this.plugin = plugin;
		this.loader = plugin.getClass().getClassLoader();
		this.path = packageBase.replace('.', '/');
		
		if (plugin == null || loader == null) {
			ProjectKorra.log.severe("Could not find classloader! Will not load abilities from " + packageBase);
			return;
		}
		
	   
		try {
			Enumeration<URL> resources = this.loader.getResources(path);
			
			String jarloc = resources.nextElement().getPath();
			jarloc = jarloc.substring(5, jarloc.length() - path.length() - 2);

			String s = URLDecoder.decode(jarloc, "UTF-8");
				
	        jar = new JarFile(new File(s));
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a list of loaded objects of the provided classType.
	 * 
	 * @param classType Type of class to load
	 * @param parentClass Type of class that the class must extend. Use {@code Object.class}
	 * for classes without a type.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> load(Class<?> classType, Class<?> parentClass) {
		ArrayList<T> loadables = new ArrayList<>();

		if (loader == null || jar == null) {
			return loadables;
		}
		
		Enumeration<JarEntry> entries = jar.entries();

	    while (entries.hasMoreElements()) {
	    	
	    	
	    	JarEntry entry = entries.nextElement();
	    	if (!entry.getName().endsWith(".class") || entry.getName().contains("$")) {
				continue;
			}

			String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
			if (!className.startsWith(path.replace('/', '.'))) {
				continue;
			}
			
			
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className, true, loader);
				
				if (!classType.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}				
			
				ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
				Constructor<?> objDef = parentClass.getDeclaredConstructor();
				Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
				T loadable = (T) clazz.cast(intConstr.newInstance());
				
				if (loadable == null) {
					continue;
				}

				loadables.add(loadable);
				AbilityLoadEvent<T> event = new AbilityLoadEvent<T>(plugin, loadable, jar);
				plugin.getServer().getPluginManager().callEvent(event);
			}
			catch (Exception | Error e) {
				continue;
			}
	    }
		
		return loadables;
	}

}
