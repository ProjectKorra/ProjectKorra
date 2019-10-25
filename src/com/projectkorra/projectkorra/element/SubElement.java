package com.projectkorra.projectkorra.element;

import org.bukkit.ChatColor;

public class SubElement extends Element {

	private final Element parent;

	public SubElement(int elementId, String elementName, String displayName, ChatColor color, Element parent) {
		super(elementId, elementName, displayName, color);

		this.parent = parent;
	}

	public Element getParent() {
		return this.parent;
	}
}
