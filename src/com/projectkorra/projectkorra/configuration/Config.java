package com.projectkorra.projectkorra.configuration;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * A config utility class for Project Korra. To get the config itself use
 * {@link #get()}.
 */
public class Config {

	private ProjectKorra plugin;

	private File file;
	private FileConfiguration config;

	/**
	 * Creates a new {@link Config} with the file being the configuration file.
	 * 
	 * @param file The file to create/load
	 */
	public Config(File file) {
		this.plugin = ProjectKorra.plugin;
		this.file = new File(plugin.getDataFolder() + File.separator + file);
		this.config = YamlConfiguration.loadConfiguration(this.file);
		reload();
	}

	/**
	 * Creates a file for the {@link FileConfiguration} object. If there are
	 * missing folders, this method will try to create them before create a file
	 * for the config.
	 */
	public void create() {
		if (!file.getParentFile().exists()) {
			try {
				file.getParentFile().mkdir();
				plugin.getLogger().info("Generating new directory for " + file.getName() + "!");
			}
			catch (Exception e) {
				plugin.getLogger().info("Failed to generate directory!");
				e.printStackTrace();
			}
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
				plugin.getLogger().info("Generating new " + file.getName() + "!");
			}
			catch (Exception e) {
				plugin.getLogger().info("Failed to generate " + file.getName() + "!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the {@link FileConfiguration} object from the {@link Config}.
	 * 
	 * @return the file configuration object
	 */
	public FileConfiguration get() {
		return config;
	}

	/**
	 * Reloads the {@link FileConfiguration} object. If the config object does
	 * not exist it will run {@link #create()} first before loading the config.
	 */
	public void reload() {
		create();
		try {
			config.load(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the {@link FileConfiguration} object.
	 * {@code config.options().copyDefaults(true)} is called before saving the
	 * config.
	 */
	public void save() {
		try {
			config.options().copyDefaults(true);
			config.save(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
