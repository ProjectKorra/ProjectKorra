package com.projectkorra.projectkorra;

import org.bukkit.ChatColor;

import java.util.HashMap;

public class Element {
	
	private static final HashMap<String, Element> ALL_ELEMENTS = new HashMap<>(); // Must be initialized first
	
	public static final Element AIR = new Element("Air");
	public static final Element WATER = new Element("Water");
	public static final Element EARTH = new Element("Earth");
	public static final Element FIRE = new Element("Fire");
	public static final Element CHI = new Element("Chi");
	public static final Element AVATAR = new Element("Avatar");
	public static final SubElement FLIGHT = new SubElement("Flight", AIR);
	public static final SubElement SPIRITUAL = new SubElement("Spiritual", AIR);
	public static final SubElement BLOOD = new SubElement("Blood", WATER);
	public static final SubElement HEALING = new SubElement("Healing", WATER);
	public static final SubElement ICE = new SubElement("Ice", WATER);
	public static final SubElement PLANT = new SubElement("Plant", WATER);
	public static final SubElement LAVA = new SubElement("Lava", EARTH);
	public static final SubElement METAL = new SubElement("Metal", EARTH);
	public static final SubElement SAND = new SubElement("Sand", EARTH);
	public static final SubElement LIGHTNING = new SubElement("Lightning", FIRE);
	public static final SubElement COMBUSTION = new SubElement("Combustion", FIRE);
	
	private static final Element[] ELEMENTS = {AIR, WATER, EARTH, FIRE, CHI, FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION};
	private static final Element[] MAIN_ELEMENTS = {AIR, WATER, EARTH, FIRE, CHI};
	private static final SubElement[] SUB_ELEMENTS = {FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION};
	
	private String name;

	private Element(String name) {
		this.name = name;
		ALL_ELEMENTS.put(name.toLowerCase(), this);
	}
	
	public ChatColor getColor() {
		return ChatColor.valueOf(ProjectKorra.plugin.getConfig().getString("Properties.Chat.Colors." + name));
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getColor() + getName();
	}
	
	public static Element getElement(String name) {
		if (name == null) {
			return null;
		}
		return ALL_ELEMENTS.get(name.toLowerCase());
	}
	
	public static Element[] getElements() {
		return ELEMENTS;
	}
	
	public static Element[] getMainElements() {
		return MAIN_ELEMENTS;
	}
	
	public static SubElement[] getSubElements() {
		return SUB_ELEMENTS;
	}
	
	public static class SubElement extends Element {
		
		private Element parentElement;
		
		private SubElement(String name, Element parentElement) {
			super(name);
			this.parentElement = parentElement;
		}
		
		@Override
		public ChatColor getColor() {
			return ChatColor.valueOf(ProjectKorra.plugin.getConfig().getString("Properties.Chat.Colors." + parentElement.name + "Sub"));
		}
		
		public Element getParentElement() {
			return this.parentElement;
		}
	}
}
