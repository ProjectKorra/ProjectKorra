package com.projectkorra.projectkorra.element;

import org.bukkit.ChatColor;

public class Element
{
	private final int _elementId;
	private final String _elementName;
	private final String _displayName;
	private final ChatColor _color;

	public Element(int elementId, String elementName, String displayName, ChatColor color)
	{
		_elementId = elementId;
		_elementName = elementName;
		_displayName = displayName;
		_color = color;
	}

	public int getId()
	{
		return _elementId;
	}

	public String getName()
	{
		return _elementName;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public ChatColor getColor()
	{
		return _color;
	}

	public String getColoredName()
	{
		return _color + _displayName;
	}
}
