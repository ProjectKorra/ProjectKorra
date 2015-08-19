package com.projectkorra.projectkorra;

import java.util.Arrays;

import org.bukkit.ChatColor;

public enum SubElement {

	//Air
	Flight (Element.Air), 
	SpiritualProjection (Element.Air), 

	//Water
	Bloodbending (Element.Water),  
	Healing (Element.Water),   
	Icebending (Element.Water),   
	Plantbending (Element.Water),

	// Earth
	Metalbending (Element.Earth), 
	Sandbending (Element.Earth),  
	Lavabending (Element.Earth), 

	// Fire
	Combustion (Element.Fire),  
	Lightning (Element.Fire);

	private Element element;
	
	SubElement(Element mainElement) {
		this.element = mainElement;
	}
	
	/**Returns the main element associated with the subelement
	 * @return The main element*/
	public Element getMainElement() {
		return element;
	}
	
	/**Returns the subelement's chatcolor that should be used
	 * @return The subelement's ChatColor*/
	public ChatColor getChatColor() {
		return element.getSubColor();
	}
	
	public static SubElement getType(String string) {
		for (SubElement se : SubElement.values()) {
			if (se.toString().equalsIgnoreCase(string)) {
				return se;
			}
		}
		return null;
	}

	public static SubElement getType(int index) {
		if (index == -1)
			return null;
		return Arrays.asList(values()).get(index);
	}

}
