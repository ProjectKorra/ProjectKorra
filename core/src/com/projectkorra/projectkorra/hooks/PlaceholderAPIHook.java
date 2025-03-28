package com.projectkorra.projectkorra.hooks;

import static java.util.stream.Collectors.joining;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TimeUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.Arrays;
import java.util.List;

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
			int index = bPlayer.getCurrentSlot() + 1;
			if (!params.equals("slot")) {
				index = Math.max(1, Math.min(9, Integer.parseInt(params.substring(params.length() - 1))));
			}
			final String ability = bPlayer.getAbilities().get(index);
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null) {
				return "";
			}
			return coreAbil.getElement().getColor() + coreAbil.getName();
		} else if (params.equals("element") || params.equals("elementcolor") || params.equals("element_title") || params.equals("element_prefix")) {
			String e = "Nonbender";
			ChatColor c = ChatColor.WHITE;
			String title = ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender", c + "[Nonbender]");
			if (player.hasPermission("bending.avatar") || (bPlayer.hasElement(Element.AIR) && bPlayer.hasElement(Element.EARTH) && bPlayer.hasElement(Element.FIRE) && bPlayer.hasElement(Element.WATER))) {
				c = Element.AVATAR.getColor();
				e = Element.AVATAR.getName();
				title = ConfigManager.languageConfig.get().getString("Chat.Prefixes.Avatar", c + "[Avatar]");
			} else if (bPlayer.getElements().size() > 0) {
				c = bPlayer.getElements().get(0).getColor();
				e = bPlayer.getElements().get(0).getName();
				title = ConfigManager.languageConfig.get().getString("Chat.Prefixes." + e, c + "[" + e + "]");
			}
			if (params.equals("element")) {
				return e;
			} else if (params.equals("elementcolor")) {
				return c.toString();
			} else {
				return title;
			}
		} else if (params.equals("elements")) {
			return bPlayer.getElements().stream().map(item -> item.getColor() + item.getName()).collect(joining(" "));
		} else if (params.equals("subelements")) {
			return bPlayer.getSubElements().stream().map(item -> item.getColor() + item.getName()).collect(joining(" "));
		} else if (params.startsWith("cooldown_")) {
			String string = params.substring("cooldown_".length());

			if (string.startsWith("slot")) {
				int index = bPlayer.getCurrentSlot() + 1;
				if (!string.equals("slot")) {
					index = Math.max(1, Math.min(9, Integer.parseInt(string.substring(string.length() - 1))));
				}
				final String ability = bPlayer.getAbilities().get(index);
				return TimeUtil.formatTime(bPlayer.getCooldown(ability) == -1 ? 0 : bPlayer.getCooldown(ability) - System.currentTimeMillis());
			} else if (string.equals("choose") || string.equals("rechoose")) {
				return TimeUtil.formatTime(bPlayer.getCooldown("RechooseCooldown") == -1 ? 0 : bPlayer.getCooldown("RechooseCooldown") - System.currentTimeMillis());
			} else {
				CoreAbility abil = CoreAbility.getAbility(string);
				if (abil != null) {
					return TimeUtil.formatTime(bPlayer.getCooldown(string) == -1 ? 0 : bPlayer.getCooldown(string) - System.currentTimeMillis());
				}
			}
		}

		return null;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return Bukkit.getPluginManager().isPluginEnabled("ProjectKorra");
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

	@Override
	public List<String> getPlaceholders() {
		return Arrays.asList("slot", "slot1", "slot2", "slot3", "slot4", "slot5", "slot6", "slot7", "slot8", "slot9",
				"element", "elementcolor", "elements", "subelements", "element_prefix",
				"cooldown_<ability>", "cooldown_slot", "cooldown_slot<1-9>", "cooldown_choose");
	}
}
