package com.projectkorra.projectkorra.element;

import com.google.common.base.Preconditions;
import com.projectkorra.projectkorra.module.DatabaseModule;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ElementManager extends DatabaseModule<ElementRepository> {

	private static final String WATER = "water", EARTH = "earth", FIRE = "fire", AIR = "air", CHI = "chi", AVATAR = "avatar";
	private static final String BLOOD = "blood", HEALING = "healing", ICE = "ice", PLANT = "plant";
	private static final String LAVA = "lava", METAL = "metal", SAND = "sand";
	private static final String COMBUSTION = "combustion", LIGHTNING = "lightning";
	private static final String FLIGHT = "flight", SPIRITUAL = "spiritual";

	private final BendingPlayerManager bendingPlayerManager;

	private final Map<Integer, Element> elements = new HashMap<>();
	private final Map<String, Element> names = new HashMap<>();

	private final String nameRegex = "[a-zA-Z]+";

	private Element water, earth, fire, air, chi, avatar;
	private SubElement blood, healing, ice, plant;
	private SubElement lava, metal, sand;
	private SubElement combustion, lightning;
	private SubElement flight, spiritual;

	private ElementManager() {
		super("Element", new ElementRepository());

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);

		runAsync(() -> {
			try {
				getRepository().createTables();

				// Waterbending
				this.water = addElement(WATER, "Water", ChatColor.AQUA, ElementType.BENDING);
				this.blood = addSubElement(BLOOD, "Blood", ChatColor.DARK_AQUA, ElementType.BENDING, this.water);
				this.healing = addSubElement(HEALING, "Healing", ChatColor.DARK_AQUA, ElementType.NO_SUFFIX, this.water);
				this.ice = addSubElement(ICE, "Ice", ChatColor.DARK_AQUA, ElementType.BENDING, this.water);
				this.plant = addSubElement(PLANT, "Plant", ChatColor.DARK_AQUA, ElementType.BENDING, this.water);

				// Earthbending
				this.earth = addElement(EARTH, "Earth", ChatColor.AQUA, ElementType.BENDING);
				this.lava = addSubElement(LAVA, "Lava", ChatColor.DARK_GREEN, ElementType.BENDING, this.earth);
				this.metal = addSubElement(METAL, "Metal", ChatColor.DARK_GREEN, ElementType.BENDING, this.earth);
				this.sand = addSubElement(SAND, "Sand", ChatColor.DARK_GREEN, ElementType.BENDING, this.earth);

				// Firebending
				this.fire = addElement(FIRE, "Fire", ChatColor.RED, ElementType.BENDING);
				this.combustion = addSubElement(COMBUSTION, "Combustion", ChatColor.DARK_RED, ElementType.BENDING, this.fire);
				this.lightning = addSubElement(LIGHTNING, "Lightning", ChatColor.DARK_RED, ElementType.BENDING, this.fire);

				// Airbending
				this.air = addElement(AIR, "Air", ChatColor.GRAY, ElementType.BENDING);
				this.flight = addSubElement(FLIGHT, "Flight", ChatColor.DARK_GRAY, ElementType.NO_SUFFIX, this.air);
				this.spiritual = addSubElement(SPIRITUAL, "Spiritual", ChatColor.DARK_GRAY, ElementType.NO_SUFFIX, this.air);

				// Chiblocking
				this.chi = addElement(CHI, "Chi", ChatColor.GOLD, ElementType.BLOCKING);

				// Avatar
				this.avatar = addElement(AVATAR, "Avatar", ChatColor.DARK_PURPLE, null);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			runSync(() -> {
				log("Populated element database tables.");
			});
		});
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event) {
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() -> {
			try {
				List<Element> elements = getRepository().selectPlayerElements(bendingPlayer.getId()).stream().map(this.elements::get).collect(Collectors.toList());

				elements.forEach(bendingPlayer::addElement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public boolean addElement(Player player, Element element) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!bendingPlayer.addElement(element)) {
			return false;
		}

		runAsync(() -> {
			try {
				getRepository().insertPlayerElement(bendingPlayer.getId(), element.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return true;
	}

	public void setElement(Player player, Element element) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		bendingPlayer.clearElements();
		bendingPlayer.addElement(element);

		runAsync(() -> {
			try {
				getRepository().deletePlayerElements(bendingPlayer.getId());
				getRepository().insertPlayerElement(bendingPlayer.getId(), element.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public boolean removeElement(Player player, Element element) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!bendingPlayer.removeElement(element)) {
			return false;
		}

		runAsync(() -> {
			try {
				getRepository().deletePlayerElement(bendingPlayer.getId(), element.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return true;
	}

	public void clearElements(Player player) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		bendingPlayer.clearElements();

		runAsync(() -> {
			try {
				getRepository().deletePlayerElements(bendingPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public Element getElement(String elementName) {
		return this.names.get(elementName);
	}

	private Element addElement(String elementName, String displayName, ChatColor color, ElementType type) {
		int elementId = registerElement(elementName);

		Element element = new Element(elementId, elementName, displayName, color, type);

		this.elements.put(elementId, element);
		this.names.put(elementName, element);

		return element;
	}

	private SubElement addSubElement(String elementName, String displayName, ChatColor color, ElementType type, Element parent) {
		int elementId = registerElement(elementName);

		SubElement element = new SubElement(elementId, elementName, displayName, color, type, parent);

		this.elements.put(elementId, element);
		this.names.put(elementName, element);

		return element;
	}

	private int registerElement(String elementName) {
		Preconditions.checkNotNull(elementName, "Element name cannot be null");

		Preconditions.checkArgument(Pattern.matches(this.nameRegex, elementName), "Element name must only contain letters and spaces");

		try {
			return getRepository().selectElemenetId(elementName);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public Element getWater() {
		return this.water;
	}

	public SubElement getBlood() {
		return this.blood;
	}

	public SubElement getHealing() {
		return this.healing;
	}

	public SubElement getIce() {
		return this.ice;
	}

	public SubElement getPlant() {
		return this.plant;
	}

	public Element getEarth() {
		return this.earth;
	}

	public SubElement getLava() {
		return this.lava;
	}

	public SubElement getMetal() {
		return this.metal;
	}

	public SubElement getSand() {
		return this.sand;
	}

	public Element getFire() {
		return this.fire;
	}

	public SubElement getCombustion() {
		return this.combustion;
	}

	public SubElement getLightning() {
		return this.lightning;
	}

	public Element getAir() {
		return this.air;
	}

	public SubElement getFlight() {
		return this.flight;
	}

	public SubElement getSpiritual() {
		return this.spiritual;
	}

	public Element getChi() {
		return this.chi;
	}

	public Element getAvatar() {
		return this.avatar;
	}

	public List<Element> getElements() {
		return new ArrayList<>(this.elements.values());
	}

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
}
