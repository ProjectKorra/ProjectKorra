package com.projectkorra.projectkorra.element;

import org.bukkit.ChatColor;

public class Element {

	private final int elementId;
	private final String elementName;
	private final String displayName;
	private final ChatColor color;
	private final ChatColor secondaryColor;
	private final ElementManager.ElementType type;

	public Element(int elementId, String elementName, String displayName, ChatColor color, ChatColor secondaryColor, ElementManager.ElementType type) {
		this.elementId = elementId;
		this.elementName = elementName;
		this.displayName = displayName;
		this.color = color;
		this.secondaryColor = secondaryColor;
		this.type = type;
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

	public ChatColor getSecondaryColor() {
		return this.secondaryColor;
	}

	public String getColoredName() {
		return this.color + this.displayName;
	}

	public ElementManager.ElementType getType() {
		return this.type;
	}
}
