package com.projectkorra.ProjectKorra.configuration;

/**
 * Represents something that loads values from configs.
 */
public interface ConfigLoadable {
	
	Config config = ConfigManager.defaultConfig;

//	public FileConfiguration getConfig();
	
	/**
	 * Reload/Loads variables from the configuration.
	 */
	public void reloadVariables();
	
}
