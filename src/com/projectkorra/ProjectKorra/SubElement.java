package com.projectkorra.ProjectKorra;

import java.util.Arrays;

public enum SubElement {

	//Air
	Flight, SpiritualProjection,

	//Water
	Bloodbending, Healing, Icebending, Plantbending,

	// Earth
	Metalbending, Sandbending, Lavabending,

	// Fire
	Combustion, Lightning;

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
