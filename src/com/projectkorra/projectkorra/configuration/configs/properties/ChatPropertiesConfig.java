package com.projectkorra.projectkorra.configuration.configs.properties;

import org.bukkit.ChatColor;

import com.projectkorra.projectkorra.configuration.Config;

public class ChatPropertiesConfig implements Config {

	public final boolean Enabled = true;
	public final String Format = "<name>: <message>";
	
	public final String AvatarPrefix = "[Avatar]";
	public final ChatColor AvatarColor = ChatColor.DARK_PURPLE;
	
	public final String AirPrefix = "[Air]";
	public final ChatColor AirColor = ChatColor.GRAY;
	public final ChatColor AirSubColor = ChatColor.DARK_GRAY;
	
	public final String EarthPrefix = "[Earth]";
	public final ChatColor EarthColor = ChatColor.GREEN;
	public final ChatColor EarthSubColor = ChatColor.DARK_GREEN;
	
	public final String FirePrefix = "[Fire]";
	public final ChatColor FireColor = ChatColor.RED;
	public final ChatColor FireSubColor = ChatColor.DARK_RED;
	
	public final String WaterPrefix = "[Water]";
	public final ChatColor WaterColor = ChatColor.AQUA;
	public final ChatColor WaterSubColor = ChatColor.DARK_AQUA;
	
	public final String ChiPrefix = "[Chi]";
	public final ChatColor ChiColor = ChatColor.GOLD;
	
	public final String NonbenderPrefix = "[Nonbender]";
	
	@Override
	public String getName() {
		return "Chat";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}