package com.projectkorra.projectkorra.configuration.better;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectkorra.projectkorra.ProjectKorra;

public class ConfigManager {
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<Class<? extends Config>, Config> CONFIG_CACHE = Collections.synchronizedMap(new HashMap<>());
	
	static {
		JavaPlugin.getProvidingPlugin(ConfigManager.class).getDataFolder().mkdir();
	}
	
	private static <C extends Config> C loadConfig(File file, Class<C> clazz) throws IOException {
		try (BufferedReader reader = Files.newReader(file, Charset.defaultCharset())) {
			return GSON.fromJson(reader, clazz);
		}
	}
	
	private static <C extends Config> void saveConfig(File file, C config) throws IOException {
		try (BufferedWriter writer = Files.newWriter(file, Charset.defaultCharset())) {
			GSON.toJson(config, writer);
		}
	}
	
	public static void clearCache() {
		CONFIG_CACHE.clear();
	}
	
	@SuppressWarnings("unchecked")
	public static <C extends Config> C getConfig(Class<C> clazz) {
		if (CONFIG_CACHE.containsKey(clazz))
		{
			return (C) CONFIG_CACHE.get(clazz);
		}
		
		try {
			C defaultConfig = clazz.newInstance();
			CONFIG_CACHE.put(clazz, defaultConfig);
			
			File file = new File(JavaPlugin.getPlugin(ProjectKorra.class).getDataFolder(), "config");
			file.mkdirs();
			
			for (String parent : defaultConfig.getParents()) {
				file = new File(file, parent);
				file.mkdir();
			}
			
			file = new File(file, defaultConfig.getName() + ".json");
			
			if (file.exists()) {
				try {
					C config = loadConfig(file, clazz);
					
					CONFIG_CACHE.put(clazz, config);
					return config;
				} catch (IOException e) {
					e.printStackTrace();
					
					return defaultConfig;
				}
			} else {
				try {
					saveConfig(file, defaultConfig);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return defaultConfig;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			
			return null;
		}
	}
}