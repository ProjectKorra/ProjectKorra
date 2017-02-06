package com.projectkorra.projectkorra;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Element {

	public enum ElementType {
		BENDING("bending", "bender", "bend"), BLOCKING("blocking", "blocker", "block"), NO_SUFFIX("", "", "");

		private String bending;
		private String bender;
		private String bend;

		ElementType(String bending, String bender, String bend) {
			this.bending = bending;
			this.bender = bender;
			this.bend = bend;
		}

		public String getBending() {
			return bending;
		}

		public String getBender() {
			return bender;
		}

		public String getBend() {
			return bend;
		}
	}

	private static final HashMap<String, Element> ALL_ELEMENTS = new HashMap<>(); // Must be initialized first

	public static final Element AIR = new Element("Air");
	public static final Element WATER = new Element("Water");
	public static final Element EARTH = new Element("Earth");
	public static final Element FIRE = new Element("Fire");
	public static final Element CHI = new Element("Chi", ElementType.BLOCKING);
	public static final Element AVATAR = new Element("Avatar", null);
	public static final SubElement FLIGHT = new SubElement("Flight", AIR, ElementType.NO_SUFFIX);
	public static final SubElement SPIRITUAL = new SubElement("Spiritual", AIR, ElementType.NO_SUFFIX);
	public static final SubElement BLOOD = new SubElement("Blood", WATER);
	public static final SubElement HEALING = new SubElement("Healing", WATER, ElementType.NO_SUFFIX);
	public static final SubElement ICE = new SubElement("Ice", WATER);
	public static final SubElement PLANT = new SubElement("Plant", WATER);
	public static final SubElement LAVA = new SubElement("Lava", EARTH);
	public static final SubElement METAL = new SubElement("Metal", EARTH);
	public static final SubElement SAND = new SubElement("Sand", EARTH);
	public static final SubElement LIGHTNING = new SubElement("Lightning", FIRE);
	public static final SubElement COMBUSTION = new SubElement("Combustion", FIRE);

	private static final Element[] ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI, FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION };
	private static final Element[] MAIN_ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI };
	private static final SubElement[] SUB_ELEMENTS = { FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION };

	private String name;
	private ElementType type;
	private Plugin plugin;

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 * 
	 * @param name Name of the new Element.
	 */
	public Element(String name) {
		this(name, ElementType.BENDING, ProjectKorra.plugin);
	}

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 * 
	 * @param name Name of the new Element.
	 * @param type ElementType specifies if its a regular element or chi style
	 *            element.
	 */
	public Element(String name, ElementType type) {
		this(name, type, ProjectKorra.plugin);
	}

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 * 
	 * @param name Name of the new Element.
	 * @param type ElementType specifies if its a regular element or chi style
	 *            element.
	 * @param plugin The plugin that is adding the element.
	 */
	public Element(String name, ElementType type, Plugin plugin) {
		this.name = name;
		this.type = type;
		this.plugin = plugin;
		ALL_ELEMENTS.put(name.toLowerCase(), this);
	}

	public String getPrefix() {
		String name_ = name;
		if (this instanceof SubElement)
			name_ = ((SubElement) this).parentElement.name;
		return getColor() + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes." + name_)) + " ";
	}

	public ChatColor getColor() {
		String color = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get().getString("Chat.Colors." + name) : plugin.getConfig().getString("Chat.Colors." + name);
		return color != null ? ChatColor.valueOf(color) : ChatColor.WHITE;
	}

	public ChatColor getSubColor() {
		String color = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get().getString("Chat.Colors." + name + "Sub") : plugin.getConfig().getString("Chat.Colors." + name + "Sub");
		return color != null ? ChatColor.valueOf(color) : ChatColor.WHITE;
	}

	public String getName() {
		return name;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public ElementType getType() {
		if (type == null)
			return ElementType.NO_SUFFIX;
		return type;
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

	/**
	 * Returns an array of all official and addon elements excluding
	 * subelements.
	 * 
	 * @return Array of all official and addon elements.
	 */
	public static Element[] getAllElements() {
		List<Element> ae = new ArrayList<Element>();
		ae.addAll(Arrays.asList(getMainElements()));
		for (Element e : ALL_ELEMENTS.values()) {
			if (!ae.contains(e) && !(e instanceof SubElement)) {
				ae.add(e);
			}
		}
		return ae.toArray(new Element[ae.size()]);
	}

	/**
	 * Returns an array of all the official elements and subelements.
	 * 
	 * @return Array of all official elements and subelements.
	 */
	public static Element[] getElements() {
		return ELEMENTS;
	}

	/**
	 * Returns an array of all the official elements.
	 * 
	 * @return Array of all official elements.
	 */
	public static Element[] getMainElements() {
		return MAIN_ELEMENTS;
	}

	/**
	 * Returns an array of all the addon elements.
	 * 
	 * @return Array of all addon elements.
	 */
	public static Element[] getAddonElements() {
		List<Element> ae = new ArrayList<Element>();
		for (Element e : getAllElements()) {
			if (!Arrays.asList(getMainElements()).contains(e)) {
				ae.add(e);
			}
		}
		ae.remove(Element.AVATAR);
		return ae.toArray(new Element[ae.size()]);
	}

	/**
	 * Returns all subelements, official and addon.
	 * 
	 * @return Array of all the subelements.
	 */
	public static SubElement[] getAllSubElements() {
		List<SubElement> se = new ArrayList<SubElement>();
		se.addAll(Arrays.asList(getSubElements()));
		for (Element e : ALL_ELEMENTS.values()) {
			if (!se.contains(e) && e instanceof SubElement) {
				se.add((SubElement) e);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	/**
	 * Return official subelements.
	 * 
	 * @return Array of official subelements.
	 */
	public static SubElement[] getSubElements() {
		return SUB_ELEMENTS;
	}

	/**
	 * Return all subelements belonging to a parent element.
	 * 
	 * @param element
	 * @return Array of all subelements belonging to a parent element.
	 */
	public static SubElement[] getSubElements(Element element) {
		List<SubElement> se = new ArrayList<SubElement>();
		for (SubElement sub : getAllSubElements()) {
			if (sub.getParentElement().equals(element)) {
				se.add(sub);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	/**
	 * Returns an array of all the addon subelements.
	 * 
	 * @return Array of all addon subelements.
	 */
	public static SubElement[] getAddonSubElements() {
		List<SubElement> ae = new ArrayList<SubElement>();
		for (SubElement e : getAllSubElements()) {
			if (!Arrays.asList(getSubElements()).contains(e)) {
				ae.add(e);
			}
		}
		return ae.toArray(new SubElement[ae.size()]);
	}

	/**
	 * Returns array of addon subelements belonging to a parent element.
	 * 
	 * @param element
	 * @return Array of addon subelements belonging to a parent element.
	 */
	public static SubElement[] getAddonSubElements(Element element) {
		List<SubElement> se = new ArrayList<SubElement>();
		for (SubElement sub : getAllSubElements()) {
			if (sub.getParentElement().equals(element) && !Arrays.asList(getSubElements()).contains(sub)) {
				se.add(sub);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	public static Element fromString(String element) {
		if (element == null || element.equals("")) {
			return null;
		}
		if (getElement(element) != null) {
			return getElement(element);
		}
		for (String s : ALL_ELEMENTS.keySet()) {
			if (element.length() <= 1 && getElement(s) instanceof SubElement) {
				continue;
			}
			if (s.length() >= element.length()) {
				if (s.substring(0, element.length()).equalsIgnoreCase(element)) {
					return getElement(s);
				}
			}
		}
		return null;
	}

	public static class SubElement extends Element {

		private Element parentElement;

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 * 
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 */
		public SubElement(String name, Element parentElement) {
			this(name, parentElement, ElementType.BENDING, ProjectKorra.plugin);
		}

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 * 
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 * @param type ElementType specifies if its a regular element or chi
		 *            style element.
		 */
		public SubElement(String name, Element parentElement, ElementType type) {
			this(name, parentElement, type, ProjectKorra.plugin);
		}

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 * 
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 * @param type ElementType specifies if its a regular element or chi
		 *            style element.
		 * @param plugin The plugin that is adding the element.
		 */
		public SubElement(String name, Element parentElement, ElementType type, Plugin plugin) {
			super(name, type, plugin);
			this.parentElement = parentElement;
		}

		@Override
		public ChatColor getColor() {
			String color = getPlugin().getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get().getString("Chat.Colors." + parentElement.name + "Sub") : getPlugin().getConfig().getString("Chat.Colors." + parentElement.name + "Sub");
			return color != null ? ChatColor.valueOf(color) : ChatColor.WHITE;
		}

		public Element getParentElement() {
			return this.parentElement;
		}
	}
}
