package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class AirAbility extends CoreAbility {

	public AirAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public AirAbility(Player player) {
		this(player, false);
	}

	@Override
	public final String getElementName() {
		return "Air";
	}

<<<<<<< HEAD
=======
	@Override
	public final ChatColor getElementColor() {
		return getAirColor();
	}

>>>>>>> Add new CoreAbility API, Add BendingPlayer methods
	/**
	 * Gets the AirColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getAirColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Air"));
	}

}
