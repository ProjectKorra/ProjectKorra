package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class EarthAbility extends CoreAbility implements SourceAbility {
	private boolean canAutoSource;
	private boolean canDynamicSource;
	private boolean canSelfSource;
	
	public EarthAbility(Player player, boolean autoStart) {
		super(player, autoStart);
	}

	public EarthAbility(Player player) {
		this(player, false);
	}

	@Override
	public final String getElementName() {
		return "Earth";
	}

	/**
	 * Gets the EarthColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getEarthColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Earth"));
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
