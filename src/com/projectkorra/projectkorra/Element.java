package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.ChatPropertiesConfig;

public class Element {

	public enum ElementType {
		BENDING("bending", "bender", "bend"), BLOCKING("blocking", "blocker", "block"), NO_SUFFIX("", "", "");

		private String bending;
		private String bender;
		private String bend;

		ElementType(final String bending, final String bender, final String bend) {
			this.bending = bending;
			this.bender = bender;
			this.bend = bend;
		}

		public String getBending() {
			return this.bending;
		}

		public String getBender() {
			return this.bender;
		}

		public String getBend() {
			return this.bend;
		}
	}

	private static final HashMap<String, Element> ALL_ELEMENTS = new HashMap<>(); // Must be initialized first.

	public static final Element AIR = new Element("Air", () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AirPrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AirColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AirSubColor);
	public static final Element WATER = new Element("Water", () -> ConfigManager.getConfig(ChatPropertiesConfig.class).WaterPrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).WaterColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).WaterSubColor);
	public static final Element EARTH = new Element("Earth", () -> ConfigManager.getConfig(ChatPropertiesConfig.class).EarthPrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).EarthColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).EarthSubColor);
	public static final Element FIRE = new Element("Fire", () -> ConfigManager.getConfig(ChatPropertiesConfig.class).FirePrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).FireColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).FireSubColor);
	public static final Element CHI = new Element("Chi", ElementType.BLOCKING, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).ChiPrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).ChiColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).ChiColor);
	public static final Element AVATAR = new Element("Avatar", null, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AvatarPrefix, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AvatarColor, () -> ConfigManager.getConfig(ChatPropertiesConfig.class).AvatarColor);
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

	private final String name;
	private final ElementType type;
	private final Plugin plugin;
	
	private final Supplier<String> prefixSupplier;
	private final Supplier<ChatColor> colorSupplier;
	private final Supplier<ChatColor> subColorSupplier;

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 *
	 * @param name Name of the new Element.
	 */
	public Element(final String name, Supplier<String> prefixSupplier, Supplier<ChatColor> colorSupplier, Supplier<ChatColor> subColorSupplier) {
		this(name, ElementType.BENDING, ProjectKorra.plugin, prefixSupplier, colorSupplier, subColorSupplier);
	}

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 *
	 * @param name Name of the new Element.
	 * @param type ElementType specifies if its a regular element or chi style
	 *            element.
	 */
	public Element(final String name, final ElementType type, Supplier<String> prefixSupplier, Supplier<ChatColor> colorSupplier, Supplier<ChatColor> subColorSupplier) {
		this(name, type, ProjectKorra.plugin, prefixSupplier, colorSupplier, subColorSupplier);
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
	public Element(final String name, final ElementType type, final Plugin plugin, Supplier<String> prefixSupplier, Supplier<ChatColor> colorSupplier, Supplier<ChatColor> subColorSupplier) {
		this.name = name;
		this.type = type;
		this.plugin = plugin;
		
		this.prefixSupplier = prefixSupplier;
		this.colorSupplier = colorSupplier;
		this.subColorSupplier = subColorSupplier;
		
		ALL_ELEMENTS.put(name.toLowerCase(), this);
	}

	public String getPrefix() {
		return this.getColor() + ChatColor.translateAlternateColorCodes('&', prefixSupplier.get()) + " ";
	}

	public ChatColor getColor() {
		return Optional.ofNullable(colorSupplier.get()).orElse(ChatColor.WHITE);
	}

	public ChatColor getSubColor() {
		return Optional.ofNullable(subColorSupplier.get()).orElse(ChatColor.WHITE);
	}

	public String getName() {
		return this.name;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public ElementType getType() {
		if (this.type == null) {
			return ElementType.NO_SUFFIX;
		}
		return this.type;
	}

	@Override
	public String toString() {
		return this.getColor() + this.getName();
	}

	public static Element getElement(final String name) {
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
		final List<Element> ae = new ArrayList<Element>();
		ae.addAll(Arrays.asList(getMainElements()));
		for (final Element e : ALL_ELEMENTS.values()) {
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
		final List<Element> ae = new ArrayList<Element>();
		for (final Element e : getAllElements()) {
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
		final List<SubElement> se = new ArrayList<SubElement>();
		se.addAll(Arrays.asList(getSubElements()));
		for (final Element e : ALL_ELEMENTS.values()) {
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
	public static SubElement[] getSubElements(final Element element) {
		final List<SubElement> se = new ArrayList<SubElement>();
		for (final SubElement sub : getAllSubElements()) {
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
		final List<SubElement> ae = new ArrayList<SubElement>();
		for (final SubElement e : getAllSubElements()) {
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
	public static SubElement[] getAddonSubElements(final Element element) {
		final List<SubElement> se = new ArrayList<SubElement>();
		for (final SubElement sub : getAllSubElements()) {
			if (sub.getParentElement().equals(element) && !Arrays.asList(getSubElements()).contains(sub)) {
				se.add(sub);
			}
		}
		return se.toArray(new SubElement[se.size()]);
	}

	public static Element fromString(final String element) {
		if (element == null || element.equals("")) {
			return null;
		}
		if (getElement(element) != null) {
			return getElement(element);
		}
		for (final String s : ALL_ELEMENTS.keySet()) {
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

		private final Element parentElement;

		/**
		 * To be used when creating a new SubElement. Do not use for comparing
		 * SubElements.
		 *
		 * @param name Name of the new SubElement.
		 * @param parentElement ParentElement of the SubElement.
		 */
		public SubElement(final String name, final Element parentElement) {
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
		public SubElement(final String name, final Element parentElement, final ElementType type) {
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
		public SubElement(final String name, final Element parentElement, final ElementType type, final Plugin plugin) {
			super(name, type, plugin, parentElement.prefixSupplier, parentElement.subColorSupplier, parentElement.subColorSupplier);
			this.parentElement = parentElement;
		}
		
		@Override
		public String getPrefix() {
			return this.getColor() + ChatColor.translateAlternateColorCodes('&', parentElement.prefixSupplier.get()) + " ";
		}

		@Override
		public ChatColor getColor() {
			return parentElement.getSubColor();
		}

		public Element getParentElement() {
			return this.parentElement;
		}
	}
}
