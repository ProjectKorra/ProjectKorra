package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.Experimental;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.configuration.ConfigManager;

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
	public static final SubElement BLUE_FIRE = new SubElement("BlueFire", FIRE);

	private static final Element[] ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI, FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION, BLUE_FIRE };
	private static final Element[] MAIN_ELEMENTS = { AIR, WATER, EARTH, FIRE, CHI };
	private static final SubElement[] SUB_ELEMENTS = { FLIGHT, SPIRITUAL, BLOOD, HEALING, ICE, PLANT, LAVA, METAL, SAND, LIGHTNING, COMBUSTION, BLUE_FIRE };

	protected final String name;
	protected final ElementType type;
	protected final Plugin plugin;
	protected ChatColor color;
	protected ChatColor subColor;
	protected boolean countsTowardsAvatar = true;

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 *
	 * @param name Name of the new Element.
	 */
	public Element(final String name) {
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
	public Element(final String name, final ElementType type) {
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
	public Element(final String name, final ElementType type, final Plugin plugin) {
		this(name, type, plugin, true);
	}

	/**
	 * To be used when creating a new Element. Do not use for comparing
	 * Elements.
	 *
	 * @param name Name of the new Element.
	 * @param type ElementType specifies if its a regular element or chi style
	 *            element.
	 * @param plugin The plugin that is adding the element.
	 * @param countTowardsAvatar If this element is used when calculating if a player has avatar
	 */
	@Experimental
	public Element(final String name, final ElementType type, final Plugin plugin, boolean countTowardsAvatar) {
		this.name = name;
		this.type = type;
		this.plugin = plugin;
		this.countsTowardsAvatar = countTowardsAvatar;
		ALL_ELEMENTS.put(name.toLowerCase(), this);
	}

	public String getPrefix() {
		String name_ = this.name;
		if (this instanceof SubElement) {
			name_ = ((SubElement) this).parentElement.name;
		}
		return this.getColor() + ChatUtil.color(ConfigManager.languageConfig.get().getString("Chat.Prefixes." + name_, this.name)) + " ";
	}

	public ChatColor getColor() {
		if (this.color == null) {
			FileConfiguration config = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get() : this.plugin.getConfig();
			String key = "Chat.Colors." + this.name;
			String value = config.getString(key);

			if (value == null && this instanceof SubElement && !(this instanceof MultiSubElement)) {
				this.color = ((SubElement) this).parentElement.getSubColor();
				return this.color;
			} else if (value == null) value = "WHITE";

			try {
				this.color = ChatColor.of(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		return this.color != null ? this.color : ChatColor.WHITE;
	}

	public ChatColor getSubColor() {
		if (this.subColor == null) {
			FileConfiguration config = this.plugin.getName().equalsIgnoreCase("ProjectKorra") ? ConfigManager.languageConfig.get() : this.plugin.getConfig();
			String key = "Chat.Colors." + this.name + "Sub";
			String value = config.getString(key);

			if (value == null && this instanceof SubElement && !(this instanceof MultiSubElement)) {
				this.color = ((SubElement) this).parentElement.getSubColor();
				return this.color;
			} else if (value == null) value = getColor().getName();

			try {
				this.subColor = ChatColor.of(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return this.subColor != null ? this.subColor : ChatColor.WHITE;
	}

	void setColor(ChatColor color) {
		this.color = color;
	}

	void setSubColor(ChatColor color) {
		this.subColor = color;
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

	/**
	 * Does this element count towards avatar? If a player has two or more elements
	 * that have this as true, they will be considered an avatar.
	 * @return True if the element should count towards being an avatar. E.g. fire,
	 * water, earth, air
	 */
	@Experimental
	public boolean doesCountTowardsAvatar() {
		if (this instanceof SubElement) return false;
		return countsTowardsAvatar;
	}

	@Override
	public String toString() {
		return (this == Element.BLUE_FIRE) ? this.getColor() + "Blue Fire": this.getColor() + this.getName();
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
			if (sub.getParentElement().equals(element) || (sub instanceof MultiSubElement && ((MultiSubElement) sub).isParentElement(element))) {
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
			if ((sub.getParentElement().equals(element) || (sub instanceof MultiSubElement && ((MultiSubElement) sub).isParentElement(element))) && !Arrays.asList(getSubElements()).contains(sub)) {
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

		protected Element parentElement;

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
			super(name, type, plugin);
			this.parentElement = parentElement;
		}

		public Element getParentElement() {
			return this.parentElement;
		}
	}

	@Experimental
	public static class MultiSubElement extends SubElement {

		private Element[] parentElements;
		private final Set<Element> parentElementsSet;

		/**
		 * To be used when creating a new Element. Do not use for comparing
		 * Elements.
		 *
		 * @param name Name of the new Element.
		 */
		public MultiSubElement(String name, Element... parentElements) {
			this(name, ElementType.NO_SUFFIX, parentElements);
		}

		/**
		 * To be used when creating a new Element. Do not use for comparing
		 * Elements.
		 *
		 * @param name Name of the new Element.
		 * @param type ElementType specifies if its a regular element or chi style
		 */
		public MultiSubElement(String name, ElementType type, Element... parentElements) {
			this(name, type, ProjectKorra.plugin, parentElements);
		}

		/**
		 * To be used when creating a new Element. Do not use for comparing
		 * Elements.
		 *
		 * @param name   Name of the new Element.
		 * @param type   ElementType specifies if its a regular element or chi style
		 *               element.
		 * @param plugin The plugin that is adding the element.
		 */
		public MultiSubElement(String name, ElementType type, Plugin plugin, Element... parentElements) {
			super(name, null, type, plugin);

			if (parentElements.length == 0) throw new IllegalArgumentException("MultiSubElement must have at least one parent element.");

			this.parentElements = parentElements;
			this.parentElementsSet = new HashSet<>(Lists.newArrayList(parentElements));

			this.parentElement = parentElements[0];
		}

		public Element[] getParentElements() {
			return this.parentElements;
		}

		public boolean isParentElement(Element element) {
			return this.parentElementsSet.contains(element);
		}
	}
}
