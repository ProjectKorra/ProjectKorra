package com.projectkorra.ProjectKorra;

import java.util.Arrays;

public enum SubElement {

	//Air
	Flight(Element.Air), SpiritualProjection(Element.Air),
	
	//Water
	Bloodbending(Element.Water), Healing(Element.Water), Icebending(Element.Water), Plantbending(Element.Water),
	
	// Earth
	Metalbending(Element.Earth), Sandbending(Element.Earth), Lavabending(Element.Earth),
	
	// Fire
	Combustion(Element.Fire), Lightning(Element.Fire);
	
	public static SubElement getType(String string) {
		for (SubElement se: SubElement.values()) {
			if (se.toString().equalsIgnoreCase(string)) {
				return se;
			}
		}
		return null;
	}
	
	public static SubElement getType(int index) {
		if (index == -1) return null;
		return (SubElement)Arrays.asList(values()).get(index);
	}
	
	private Element element;
	
	private SubElement(Element e) {
		element = e;
	}
	
	public Element getElement() {
		return element;
	}

	
}
