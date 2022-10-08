package com.projectkorra.projectkorra.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBar {

	public static void sendActionBar(final String message, final Player... player) {
		for (Player e : player) {
			e.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(TextComponent.fromLegacyText(message)));
		}
	}
}
