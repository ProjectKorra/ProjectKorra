package com.projectkorra.projectkorra.hooks;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static java.util.stream.Collectors.joining;

public class PlaceholderAPIHook extends EZPlaceholderHook {

	public PlaceholderAPIHook(ProjectKorra plugin) {
		super(plugin, "projectkorra");
	}

	public String onPlaceholderRequest(Player player, String params) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) { return ""; }

		if (params.startsWith("slot")) {
			final String ability = bPlayer.getAbilities().get(Integer.parseInt(params.substring(params.length() - 1)));
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null) {
				return "";
			}
			return coreAbil.getElement().getColor() + coreAbil.getName();
		} else if (params.equals("element") || params.equals("elementcolor")) {
			String e = "Nonbender";
			ChatColor c = ChatColor.WHITE;
			if (player.hasPermission("bending.avatar") || (bPlayer.hasElement(Element.AIR) && bPlayer.hasElement(Element.EARTH) && bPlayer.hasElement(Element.FIRE) && bPlayer.hasElement(Element.WATER))) {
				c = Element.AVATAR.getColor();
				e = Element.AVATAR.getName();
			} else if (bPlayer.getElements().size() > 0) {
				c = bPlayer.getElements().get(0).getColor();
				e = bPlayer.getElements().get(0).getName();
			}
			final String element = ConfigManager.languageConfig.get().getString("Chat.Prefixes." + e);
			if (params.equals("element")) {
				return c + element + ChatColor.RESET;
			} else if(params.equals("elementcolor")) {
				return c + "";
			}
		} else if (params.equals("elements")) {
			return bPlayer.getElements().stream().map(item -> item.getColor() + item.getName()).collect(joining(" "));
		}

		return null;
	}

}
