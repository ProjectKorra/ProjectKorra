package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class WaterAbility extends CoreAbility implements SourceAbility {

	private boolean canAutoSource;
	private boolean canDynamicSource;
	private boolean canSelfSource;
	
	public WaterAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public WaterAbility(Player player) {
		this(player, false);
	}

	@Override
	public final String getElementName() {
		return "Water";
	}

	/**
	 * Gets the WaterColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getWaterColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Water"));
	}

	public boolean canAutoSource() {
		return canAutoSource;
	}
	
	public boolean canDynamicSource() {
		return canDynamicSource;
	}
	
	public boolean canSelfSource() {
		return canSelfSource;
	}
}
