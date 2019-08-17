package com.projectkorra.projectkorra.hooks;

import static java.util.stream.Collectors.joining;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.ChatPropertiesConfig;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@SuppressWarnings("rawtypes")
public class PlaceholderAPIHook extends PlaceholderExpansion {

	private final ProjectKorra plugin;

	public PlaceholderAPIHook(final ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String params) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return "";
		}

		if (params.startsWith("slot")) {
			final String ability = bPlayer.getAbilities().get(Integer.parseInt(params.substring(params.length() - 1)));
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null) {
				return "";
			}
			return coreAbil.getElement().getColor() + coreAbil.getName();
		} else if (params.equals("element") || params.equals("elementcolor")) {
			String e = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig(ChatPropertiesConfig.class).NonbenderPrefix);
			ChatColor c = ChatColor.WHITE;
			if (player.hasPermission("bending.avatar") || (bPlayer.hasElement(Element.AIR) && bPlayer.hasElement(Element.EARTH) && bPlayer.hasElement(Element.FIRE) && bPlayer.hasElement(Element.WATER))) {
				c = Element.AVATAR.getColor();
				e = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig(ChatPropertiesConfig.class).AvatarPrefix);
			} else if (bPlayer.getElements().size() > 0) {
				c = bPlayer.getElements().get(0).getColor();
				e = bPlayer.getElements().get(0).getPrefix();
			}
			if (params.equals("element")) {
				return c + e + ChatColor.RESET;
			} else if (params.equals("elementcolor")) {
				return c + "";
			}
		} else if (params.equals("elements")) {
			return bPlayer.getElements().stream().map(item -> item.getColor() + item.getName()).collect(joining(" "));
		}

		return null;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getAuthor() {
		return this.plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "ProjectKorra";
	}

	@Override
	public String getVersion() {
		return this.plugin.getDescription().getVersion();
	}
}
