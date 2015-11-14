package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.command.Commands;

import org.bukkit.ChatColor;

import java.util.Arrays;

public enum SubElement {

	//Air
	Flight (Element.Air, Commands.flightaliases), 
	SpiritualProjection (Element.Air, Commands.spiritualprojectionaliases), 

	//Water
	Bloodbending (Element.Water, Commands.bloodaliases),  
	Healing (Element.Water, Commands.healingaliases),   
	Icebending (Element.Water, Commands.icealiases),   
	Plantbending (Element.Water, Commands.plantaliases),

	// Earth
	Metalbending (Element.Earth, Commands.metalbendingaliases), 
	Sandbending (Element.Earth, Commands.sandbendingaliases),  
	Lavabending (Element.Earth, Commands.lavabendingaliases), 

	// Fire
	Combustion (Element.Fire, Commands.combustionaliases),  
	Lightning (Element.Fire, Commands.lightningaliases);

	private Element element;
	private String[] aliases;
	
	SubElement(Element mainElement, String[] aliases) {
		this.element = mainElement;
		this.aliases = aliases;
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
			if (Arrays.asList(se.aliases).contains(string.toLowerCase())) {
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
