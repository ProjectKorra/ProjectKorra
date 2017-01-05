package com.projectkorra.projectkorra.util;

import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Updater class that takes an rss feed and checks for updates there <br>
 * Will only work on xenforo rss feeds
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
 * @author Jacklin213
 *
 */
public class Updater {

	private URL url;
	private URLConnection urlc;
	private Document document;
	private String currentVersion;
	private Plugin plugin;
	private String pluginName;

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
	 */
	public Updater(Plugin plugin, String URL) {
		this.plugin = plugin;
		try {
			url = new URL(URL);
			urlc = url.openConnection();
			urlc.setRequestProperty("User-Agent", ""); // Must be used or face 403
			urlc.setConnectTimeout(30000); // 30 second time out, throws SocketTimeoutException
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlc.getInputStream());
		}
		catch (IOException e) {
			plugin.getLogger().info("Could not connect to ProjectKorra.com to check for updates");
		}
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		this.currentVersion = plugin.getDescription().getVersion();
		this.pluginName = plugin.getDescription().getName();
	}

	/**
	 * Logs and update message in console. Displays different messages dependent
	 * on {@link #updateAvailable()}
	 * 
	 */
	public void checkUpdate() {
		if (getUpdateVersion() == null) {
			return;
		} else if (updateAvailable()) {
			plugin.getLogger().info("===================[Update Available]===================");
			plugin.getLogger().info("You are running version " + getCurrentVersion());
			plugin.getLogger().info("The latest version avaliable is " + getUpdateVersion());
		} else {
			plugin.getLogger().info("You are running the latest version of " + pluginName);
		}
	}

	/**
	 * Gets latest plugin version.
	 * 
	 * @return Latest plugin version, or null if it cannot connect
	 */
	public String getUpdateVersion() {
		if (document != null) {
			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			String version = children.item(1).getTextContent();
			return version.toUpperCase();
		}
		return null;
	}

	/**
	 * Checks to see if an update is available.
	 * 
	 * @return true If there is an update
	 */
	public boolean updateAvailable() {
		String updateVersion = getUpdateVersion();
		if (updateVersion == null) {
			return false;
		}
		int currentNumber = Integer.parseInt(currentVersion.substring(0, 5).replaceAll("\\.", ""));
		int updateNumber = Integer.parseInt(updateVersion.substring(0, 5).replaceAll("\\.", ""));
		if (updateNumber == currentNumber) {
			if (currentVersion.contains("BETA") && updateVersion.contains("BETA")) {
				int currentBeta = Integer.parseInt(currentVersion.substring(currentVersion.lastIndexOf(" ") + 1));
				int updateBeta = Integer.parseInt(updateVersion.substring(updateVersion.lastIndexOf(" ") + 1));
				if (currentBeta == updateBeta || currentBeta > updateBeta) {
					return false;
				}
			} else if (!currentVersion.contains("BETA") && updateVersion.contains("BETA")) {
				return false;
			}
		} else if (currentVersion.equalsIgnoreCase(updateVersion) || currentNumber > updateNumber) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the connected URL object.
	 * 
	 * @return The URL object
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Gets the current plugin version from the plugin.yml.
	 * 
	 * @return The current plugin version
	 */
	public String getCurrentVersion() {
		return currentVersion;
	}

}
