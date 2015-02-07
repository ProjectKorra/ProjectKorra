package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Element {

	Air(1, new ArrayList<SubElement>()), Water(2, new ArrayList<SubElement>()), Earth(3, new ArrayList<SubElement>()), Fire(4, new ArrayList<SubElement>()), Chi(5, new ArrayList<SubElement>());
	
	public static Element getType(String string) {
		for (Element element: Element.values()) {
			if (element.toString().equalsIgnoreCase(string)) {
				return element;
			}
		}
		return null;
	}
	
	public static Element getType(int index) {
	    if (index == -1)
	    	return null;
	    return (Element)Arrays.asList(values()).get(index);
	}
	
	private List<SubElement> subs;
	private Element(int i, List<SubElement> su) {
		List<SubElement> s = su;
		switch(i) {
		case 1:
			s.add(SubElement.Flight);
			s.add(SubElement.SpiritualProjection);
			subs = su;
			break;
		case 2:
			s.add(SubElement.Bloodbending);
			s.add(SubElement.Healing);
			s.add(SubElement.Icebending);
			s.add(SubElement.Plantbending);
			subs = su;
			break;
		case 3:
			s.add(SubElement.Metalbending);
			s.add(SubElement.Sandbending);
			s.add(SubElement.Lavabending);
			subs = su;
			break;
		case 4:
			s.add(SubElement.Lightning);
			s.add(SubElement.Combustion);
			subs = su;
			break;
		default:
			break;
		}
	}
	
	public List<SubElement> getSubElements() {
		return subs;
	}
}
