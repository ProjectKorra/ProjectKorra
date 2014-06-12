package com.projectkorra.ProjectKorra;

public enum Element {

	Air, Water, Earth, Fire, Chi;
	
	public static Element getType(String string) {
		for (Element element: Element.values()) {
			if (element.toString().equalsIgnoreCase(string)) {
				return element;
			}
		}
		return null;
	}
}
