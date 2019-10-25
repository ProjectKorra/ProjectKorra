package com.projectkorra.projectkorra.element;

import org.bukkit.ChatColor;

public class Element {

	private final int elementId;
	private final String elementName;
	private final String displayName;
	private final ChatColor color;

	public Element(int elementId, String elementName, String displayName, ChatColor color) {
		this.elementId = elementId;
		this.elementName = elementName;
		this.displayName = displayName;
		this.color = color;
	}

	public int getId() {
		return this.elementId;
	}

	public String getName() {
		return this.elementName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public ChatColor getColor() {
		return this.color;
	}

	public String getColoredName() {
		return this.color + this.displayName;
	}
}
