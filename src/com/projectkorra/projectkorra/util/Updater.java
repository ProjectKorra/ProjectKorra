package com.projectkorra.projectkorra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.plugin.Plugin;

/**
 * Updater class that takes an rss feed and checks for updates there <br>
 * <s>Will only work on xenforo rss feeds</s> Outdated: RSS feeds no longer
 * available. Gets the version from the page itself and parse it.
 * <p>
 * Methods to look for in this class:
 * <ul>
 * <li>{@link #checkUpdate()} called in {@code plugin.onEnable()} to display
 * update message in log</li>
 * <li>{@link #getCurrentVersion()} to get the version of the plugin</li>
 * <li>{@link #getUpdateVersion()} to get the update version</li>
 * <li>{@link #updateAvailable()} to check if theres an update</li>
 * </ul>
 * </p>
 *
 * @author Jacklin213, updated by StrangeOne101
 */
public class Updater {

	private URL url;
	private URLConnection urlc;
	private String updateVersion;
	private final String currentVersion;
	private final Plugin plugin;
	private final boolean checkUpdate;
	private final String pluginName;

	/**
	 * Creates a new instance of Updater. This constructor should only be called
	 * inside of {@code plugin.onEnable()} or called after the plugin is loaded.
	 * <br>
	 * <br>
	 * This constructor should NEVER be called to initiate a field. If called to
	 * initiate a field, Updater will throw NullPointerExceptions
	 *
	 * @param plugin Plugin to check updates for
	 * @param URL RSS feed URL link to check for updates on.
	 * @param checkForUpdate Whether the plugin should check for updates when
	 *            the server starts or not. Defined in the config
	 */
	public Updater(final Plugin plugin, final String URL, final boolean checkForUpdate) {
		this.plugin = plugin;
		this.checkUpdate = checkForUpdate;
		if (this.checkUpdate) {
			this.runAsync(plugin, () -> {
				try {
					plugin.getLogger().info("Checking for updates...!");
					this.url = new URL(URL);
					this.urlc = this.url.openConnection();
					this.urlc.setRequestProperty("User-Agent", "Mozilla/5.0"); // Must be used or face 403.
					this.urlc.setConnectTimeout(30000); // 30 second time out, throws SocketTimeoutException.

					try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.urlc.getInputStream()))) {
						String line;
						while ((line = reader.readLine()) != null) {
							// The characters allowed in the version are any digit, full stops, spaces, or "for MC x.x.x+".
							// Then we are just parsing it from the line that states the version.
							if (line.toLowerCase().matches(".*<span class=\"u-muted\">[0-9\\. formcr+]{1,23}<\\/span>.*")) {
								line = line.trim();
								this.updateVersion = line.split("<span class=\"u-muted\">")[1].split("<\\/span>")[0];
								break;
							}
						}
					}
					catch (final IOException e) {
						e.printStackTrace();
					}
				}
				catch (final IOException e) {
					plugin.getLogger().info("Could not connect to projectkorra.com");
				}
				this.checkUpdate();
			});
		}
		this.currentVersion = plugin.getDescription().getVersion();
		this.pluginName = plugin.getDescription().getName();
	}

	private void runAsync(final Plugin plugin, final Runnable run) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, run);
	}

	/**
	 * Logs and update message in console. Displays different messages dependent
	 * on {@link #updateAvailable()}
	 */
	public void checkUpdate() {
		if (!this.isEnabled()) {
			return;
		}
		if (this.getUpdateVersion() == null) {
			this.plugin.getLogger().info("Something went wrong while trying to retrieve the latest version.");
			return;
		}
		if (this.updateAvailable()) {
			this.plugin.getLogger().info("===================[Update Available]===================");
			this.plugin.getLogger().info("You are running version " + this.getCurrentVersion());
			this.plugin.getLogger().info("The latest version available is " + this.getUpdateVersion());
		} else {
			this.plugin.getLogger().info("You are running the latest version of " + this.pluginName);
		}
	}

	/**
	 * Gets latest plugin version.
	 *
	 * @return Latest plugin version, or null if it cannot connect
	 */
	public String getUpdateVersion() {
		return this.updateVersion;
	}

	/**
	 * Checks to see if an update is available. <b>Note: </b> This method does
	 * <i>not</i> check the newest version in real time. It checks using the
	 * latest version number retrieved upon startup.
	 *
	 * @return true If there is an update
	 */
	public boolean updateAvailable() {
		final String updateVersion = this.getUpdateVersion();
		if (updateVersion == null) {
			return false;
		}
		final String numericUpdateVersion = updateVersion.split(" ")[0]; // Only take the left half if there is words in it too.
		final String numericCurrentVersion = this.currentVersion.split(" ")[0];
		final int currentNumber = Integer.parseInt(numericCurrentVersion.replaceAll("[^\\d]", "")); // Replace points So version is just 186 instead of 1.8.6, etc.
		final int updateNumber = Integer.parseInt(numericUpdateVersion.replaceAll("[^\\d]", ""));

		return currentNumber < updateNumber || this.currentVersion.hashCode() != updateVersion.hashCode(); // If the numeric versions are the same, check if the version string is different.
	}

	/**
	 * Gets the connected URL object.
	 *
	 * @return The URL object
	 */
	public URL getUrl() {
		return this.url;
	}

	/**
	 * Gets the current plugin version from the plugin.yml.
	 *
	 * @return The current plugin version
	 */
	public String getCurrentVersion() {
		return this.currentVersion;
	}

	/**
	 * Returns whether the update checker has been enabled or not.
	 *
	 * @return True if enabled, otherwise false
	 */
	public boolean isEnabled() {
		return this.checkUpdate;
	}

}
