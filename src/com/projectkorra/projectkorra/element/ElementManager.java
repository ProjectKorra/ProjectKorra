package com.projectkorra.projectkorra.element;

import com.google.common.base.Preconditions;
import com.projectkorra.projectkorra.module.DatabaseModule;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ElementManager extends DatabaseModule<ElementRepository>
{
	private static final String WATER = "water", EARTH = "earth", FIRE = "fire", AIR = "air", CHI = "chi", AVATAR = "avatar";
	private static final String BLOOD = "blood", HEALING = "healing", ICE = "ice", PLANT = "plant";
	private static final String LAVA = "lava", METAL = "metal", SAND = "sand";
	private static final String COMBUSTION = "combustion", LIGHTNING = "lightning";
	private static final String FLIGHT = "flight", SPIRITUAL = "spiritual";

	private final Map<Integer, Element> _elements = new HashMap<>();

	private final String _nameRegex = "[a-zA-Z]+";

	private Element water, earth, fire, air, chi, avatar;
	private SubElement blood, healing, ice, plant;
	private SubElement lava, metal, sand;
	private SubElement combustion, lightning;
	private SubElement flight, spiritual;

	private ElementManager()
	{
		super("Element", new ElementRepository());

		runAsync(() ->
		{
			try
			{
				getRepository().createTables();

				// Waterbending
				water = addElement(WATER, "Water", ChatColor.AQUA);
				blood = addSubElement(BLOOD, "Blood", ChatColor.DARK_AQUA, water);
				healing = addSubElement(HEALING, "Healing", ChatColor.DARK_AQUA, water);
				ice = addSubElement(ICE, "Ice", ChatColor.DARK_AQUA, water);
				plant = addSubElement(PLANT, "Plant", ChatColor.DARK_AQUA, water);

				// Earthbending
				earth = addElement(EARTH, "Earth", ChatColor.AQUA);
				lava =addSubElement(LAVA, "Lava", ChatColor.DARK_GREEN, earth);
				metal = addSubElement(METAL, "Metal", ChatColor.DARK_GREEN, earth);
				sand = addSubElement(SAND, "Sand", ChatColor.DARK_GREEN, earth);

				// Firebending
				fire = addElement(FIRE, "Fire", ChatColor.RED);
				combustion = addSubElement(COMBUSTION, "Combustion", ChatColor.DARK_RED, fire);
				lightning = addSubElement(LIGHTNING, "Lightning", ChatColor.DARK_RED, fire);

				// Airbending
				air = addElement(AIR, "Air", ChatColor.GRAY);
				flight = addSubElement(FLIGHT, "Flight", ChatColor.DARK_GRAY, air);
				spiritual = addSubElement(SPIRITUAL, "Spiritual", ChatColor.DARK_GRAY, air);

				// Chiblocking
				chi = addElement(CHI, "Chi",  ChatColor.GOLD);

				// Avatar
				avatar = addElement(AVATAR, "Avatar",  ChatColor.DARK_PURPLE);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}

			runSync(() ->
			{
				log("Populated element database tables.");
			});
		});
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event)
	{
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() ->
		{
			try
			{
				List<Element> elements = getRepository().selectPlayerElements(bendingPlayer.getId()).stream()
						.map(_elements::get)
						.collect(Collectors.toList());

				elements.forEach(bendingPlayer::addElement);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	private Element addElement(String elementName, String displayName, ChatColor color)
	{
		int elementId = registerElement(elementName);

		Element element = new Element(elementId, elementName, displayName, color);

		_elements.put(elementId, element);

		return element;
	}

	private SubElement addSubElement(String elementName, String displayName, ChatColor color, Element parent)
	{
		int elementId = registerElement(elementName);

		SubElement element = new SubElement(elementId, elementName, displayName, color, parent);

		_elements.put(elementId, element);

		return element;
	}

	private int registerElement(String elementName)
	{
		Preconditions.checkNotNull(elementName, "Element name cannot be null");

		Preconditions.checkArgument(Pattern.matches(_nameRegex, elementName), "Element name must only contain letters and spaces");

		try
		{
			return getRepository().selectElemenetId(elementName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public Element getWater()
	{
		return water;
	}

	public SubElement getBlood()
	{
		return blood;
	}

	public SubElement getHealing()
	{
		return healing;
	}

	public SubElement getIce()
	{
		return ice;
	}

	public SubElement getPlant()
	{
		return plant;
	}

	public Element getEarth()
	{
		return earth;
	}

	public SubElement getLava()
	{
		return lava;
	}

	public SubElement getMetal()
	{
		return metal;
	}

	public SubElement getSand()
	{
		return sand;
	}

	public Element getFire()
	{
		return fire;
	}

	public SubElement getCombustion()
	{
		return combustion;
	}

	public SubElement getLightning()
	{
		return lightning;
	}

	public Element getAir()
	{
		return air;
	}

	public SubElement getFlight()
	{
		return flight;
	}

	public SubElement getSpiritual()
	{
		return spiritual;
	}

	public Element getChi()
	{
		return chi;
	}

	public Element getAvatar()
	{
		return avatar;
	}
}
