package com.projectkorra.projectkorra.util;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBar {

	public static void sendActionBar(final String message, final Player... player) {
		for (Player e : player) {
			e.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
		}
	}
}
