package com.projectkorra.projectkorra.element;

import org.bukkit.ChatColor;

public class SubElement extends Element {

	private final Element parent;

	public SubElement(int elementId, String elementName, String displayName, ChatColor color, ElementManager.ElementType type, Element parent) {
		super(elementId, elementName, displayName, color, type);

		this.parent = parent;
	}

	public Element getParent() {
		return this.parent;
	}
}
