package com.projectkorra.projectkorra.configuration;

/**
 * Represents something that loads values from configs.
 * 
 * @author Jacklin213
 * @version 1.0.0
 */
public interface ConfigLoadable {

	Config config = ConfigManager.defaultConfig;

	/**
	 * Reload/Loads variables from the configuration.
	 */
	public void reloadVariables();

}
