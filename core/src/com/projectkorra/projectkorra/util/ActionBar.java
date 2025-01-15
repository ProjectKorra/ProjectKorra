package com.projectkorra.projectkorra.util;


import org.bukkit.entity.Player;

/**
 * Deprecated for removal. Use {@link ChatUtil#sendActionBar(String, Player...)} instead
 */
@Deprecated
public class ActionBar {

	/**
	 * Deprecated for removal. Use {@link ChatUtil#sendActionBar(String, Player...)} instead
	 * @param message The message to send
	 * @param player The player to send the message to
	 */
	public static void sendActionBar(final String message, final Player... player) {
		ChatUtil.sendActionBar(message, player);
	}

}
