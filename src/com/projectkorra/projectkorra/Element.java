package com.projectkorra.projectkorra;

import java.util.Arrays;

public enum Element {

	Air, Water, Earth, Fire, Chi;

	public static Element getType(String string) {
		for (Element element : Element.values()) {
			if (element.toString().equalsIgnoreCase(string)) {
				return element;
			}
		}
		return null;
	}

	public static Element getType(int index) {
		if (index == -1)
			return null;
		return Arrays.asList(values()).get(index);
	}
}
