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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ElementManager extends DatabaseModule<ElementRepository>
{
	private static final String WATER = "water";
	private static final String BLOOD = "blood";
	private static final String HEALING = "healing";
	private static final String EARTH = "earth";
	private static final String LAVA = "lava";
	private static final String METAL = "metal";
	private static final String SAND = "sand";
	private static final String FIRE = "fire";
	private static final String COMBUSTION = "combustion";
	private static final String LIGHTNING = "lightning";
	private static final String AIR = "air";
	private static final String FLIGHT = "flight";
	private static final String SPIRITUAL = "spiritual";
	private static final String CHI = "chi";

	private final BendingPlayerManager _bendingPlayerManager;

	private final Map<Integer, Element> _elements = new HashMap<>();
	private final Map<String, Element> _names = new HashMap<>();

	private final String _nameRegex = "[a-zA-Z]+";

	private ElementManager()
	{
		super("Element", new ElementRepository());

		_bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);

		runAsync(() ->
		{
			try
			{
				getRepository().createTables();

				// Waterbending
				Element water = addElement(WATER, "Water", ChatColor.AQUA);
				addSubElement(BLOOD, "Blood", ChatColor.DARK_AQUA, water);
				addSubElement(HEALING, "Healing", ChatColor.DARK_AQUA, water);

				// Earthbending
				Element earth = addElement(EARTH, "Earth", ChatColor.AQUA);
				addSubElement(LAVA, "Lava", ChatColor.DARK_GREEN, earth);
				addSubElement(METAL, "Metal", ChatColor.DARK_GREEN, earth);
				addSubElement(SAND, "Sand", ChatColor.DARK_GREEN, earth);

				// Firebending
				Element fire = addElement(FIRE, "Fire", ChatColor.RED);
				addSubElement(COMBUSTION, "Combustion", ChatColor.DARK_RED, fire);
				addSubElement(LIGHTNING, "Lightning", ChatColor.DARK_RED, fire);

				// Airbending
				Element air = addElement(AIR, "Air", ChatColor.GRAY);
				addSubElement(FLIGHT, "Flight", ChatColor.DARK_GRAY, air);
				addSubElement(SPIRITUAL, "Spiritual", ChatColor.DARK_GRAY, air);

				// Chiblocking
				Element chi = addElement(CHI, "Chi",  ChatColor.GOLD);
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

//				bendingPlayer.addElements(elements);
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
		_names.put(elementName, element);

		return element;
	}

	private SubElement addSubElement(String elementName, String displayName, ChatColor color, Element parent)
	{
		int elementId = registerElement(elementName);

		SubElement element = new SubElement(elementId, elementName, displayName, color, parent);

		_elements.put(elementId, element);
		_names.put(elementName, element);

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
}
