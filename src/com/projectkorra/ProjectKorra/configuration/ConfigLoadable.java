package com.projectkorra.ProjectKorra.configuration;

import org.bukkit.configuration.file.FileConfiguration;

import com.projectkorra.ProjectKorra.ProjectKorra;

/**
 * Represents something that loads values from configs.
 */
public interface ConfigLoadable {
	
	FileConfiguration config = ProjectKorra.plugin.getConfig();

	/**
	 * Reload/Loads variables from the configuration.
	 */
	public void reloadVariables();
	
}
