package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ChiMethods {

	static ProjectKorra plugin;
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public ChiMethods(ProjectKorra plugin) {
		ChiMethods.plugin = plugin;
	}

	/**
	 * Gets the ChiColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getChiColor() {
		return ChatColor.valueOf(config.getString("Properties.Chat.Colors.Chi"));
	}

	/**
	 * Checks whether an ability is a chi ability.
	 * 
	 * @param ability The ability to check
	 * @return true If the ability is a chi ability.
	 */
	public static boolean isChiAbility(String ability) {
		return AbilityModuleManager.chiabilities.contains(ability);
	}

	/**
	 * Checks whether a player is chiblocked.
	 * 
	 * @param player The player to check
	 * @return true If the player is chiblocked.
	 */
	public static boolean isChiBlocked(String player) {
		if (GeneralMethods.getBendingPlayer(player) != null) {
			return GeneralMethods.getBendingPlayer(player).isChiBlocked();
		}
		return false;
	}

	public static void stopBending() {
		RapidPunch.instances.clear();
		WarriorStance.instances.clear();
		AcrobatStance.instances.clear();
	}
}
