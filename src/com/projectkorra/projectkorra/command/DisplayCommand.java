package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Executor for /bending display. Extends {@link PKCommand}.
 */
public class DisplayCommand extends PKCommand {

	private static final Map<String, String> abbreviations = new HashMap<>();
	private final String noCombosAvailable;
	private final String noPassivesAvailable;
	private final String noAbilitiesAvailable;
	private final String invalidArgument;
	private final String playersOnly;
	private final String noBinds;
	private final String format;
	private final String separator;
	private final String hoverType;
	private final String hoverAbility;

	public DisplayCommand() {
		super("display", "/bending display <Element>", ConfigManager.languageConfig.get().getString("Commands.Display.Description"), new String[] { "display", "dis", "d" });

		this.noCombosAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAvailable");
		this.noPassivesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAvailable");
		this.noAbilitiesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAvailable");
		this.invalidArgument = ConfigManager.languageConfig.get().getString("Commands.Display.InvalidArgument");
		this.playersOnly = ConfigManager.languageConfig.get().getString("Commands.Display.PlayersOnly");
		this.noBinds = ConfigManager.languageConfig.get().getString("Commands.Display.NoBinds");
		this.format = ConfigManager.languageConfig.get().getString("Commands.Display.Format");
		this.separator = ConfigManager.languageConfig.get().getString("Commands.Display.Separator");
		this.hoverType = ConfigManager.languageConfig.get().getString("Commands.Display.HoverType");
		this.hoverAbility = ConfigManager.languageConfig.get().getString("Commands.Display.HoverAbility");
		
		this.fillAbbreviations();
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}

		// bending display [Element].
		if (args.size() == 1) {
			String elementName = args.get(0).toLowerCase().replace("bending", "");
			elementName = abbreviations.getOrDefault(elementName, elementName);
			
			final Element element = Element.fromString(elementName.replace("combos", "").replace("combo", "").replace("passives", "").replace("passive", ""));
			final ChatColor color = element == null ? null : element.getColor();
			// Combos.
			if (elementName.contains("combo")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Combos");

					for (final Element e : Element.getAllElements()) {
						final ArrayList<String> combos = this.correctCombos(sender, e);
						this.iterateAbilities(sender, combos);
					}
					return;
				}
				
				final ArrayList<String> combos = this.correctCombos(sender, element);
				
				if (combos.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + this.noCombosAvailable.replace("{element}", element.getName()));
					return;
				}
				
				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() +  " Combos");
				this.iterateAbilities(sender, combos);
				return;
				// Passives.
			} else if (elementName.contains("passive")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Passives");

					for (final Element e : Element.getAllElements()) {
						final Set<String> passives = PassiveManager.getPassivesForElement(e);
						this.iterateAbilities(sender, passives);
					}
					return;
				}
				
				final Set<String> passives = PassiveManager.getPassivesForElement(element);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + this.noPassivesAvailable.replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + " Passives");
				this.iterateAbilities(sender, passives);
				return;
			} else if (element != null) {
				this.displayElement(sender, element);
			} else {
				final StringBuilder elements = new StringBuilder(ChatColor.RED + this.invalidArgument).append(ChatColor.WHITE).append("\nElements: ");
				
				for (final Element e : Element.getAllElements()) {
					if (!(e instanceof SubElement)) {
						elements.append(e.getColor()).append(e.getName()).append(ChatColor.WHITE).append(" | ");
					}
				}
				
				final StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (final SubElement e : Element.getAllSubElements()) {
					subelements.append(ChatColor.WHITE).append("\n- ").append(e.getColor()).append(e.getName());
				}
				
				sender.sendMessage(elements.toString());
				sender.sendMessage(subelements.toString());
			}
		}
		
		if (args.isEmpty()) {
			// Bending Display.
			if (!(sender instanceof Player)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playersOnly);
				return;
			}
			
			this.displayBinds(sender);
		}
	}

	/**
	 * Iterates over a given list of Abilities (Moves, Passives, Combos) and sends them
	 *
	 * @param sender The CommandSender to show the abilities to
	 * @param abilityNames The list to iterate through
	 */
	private void iterateAbilities(final CommandSender sender, final Collection<String> abilityNames) {
		final ComponentBuilder fullMessage = new ComponentBuilder();
		for (String abilityName : abilityNames) {
			final CoreAbility coreAbil = CoreAbility.getAbility(abilityName);
			if (sender instanceof Player && !sender.hasPermission("bending.ability." + abilityName) || coreAbil == null || (! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility())) {
				continue;
			}
			
			if (!fullMessage.getParts().isEmpty()) {
				fullMessage.appendLegacy(this.color(this.separator));
			}

			final ChatColor color = coreAbil.getElement().getColor();
			final ChatColor subColor = coreAbil.getElement() instanceof SubElement ? color : coreAbil.getElement().getSubColor();
			
			fullMessage.appendLegacy(this.color(this.format.replace("{ability}", color + abilityName)));
			fullMessage.event(hoverEvent(color + this.color(this.hoverAbility.replace("{ability}", subColor + abilityName))));
			fullMessage.event(clickEvent("/bending help " + coreAbil.getName()));
		}
		sender.spigot().sendMessage(fullMessage.create());
	}

	/**
	 * Displays the enabled moves and subelements for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final Collection<CoreAbility> abilities = filterAbilities(sender, CoreAbility.getAbilitiesByElement(element));
		final String elementName = element.getName();
		final String bending = element.getType().getBending();
		final ChatColor mainColor = element.getColor();
		final ChatColor subColor = element instanceof SubElement ? mainColor : element.getSubColor();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", mainColor + elementName + ChatColor.YELLOW));
			return;
		}
		
		sender.sendMessage(mainColor + (ChatColor.BOLD + elementName) + bending);

		final HashSet<String> abilitiesSent = new HashSet<>(); // Some abilities have the same name. This prevents this from showing anything.
		for (final CoreAbility ability : abilities) {
			if ((!(element instanceof SubElement) && ability instanceof SubAbility) || ability instanceof PassiveAbility ||  ability instanceof ComboAbility || abilitiesSent.contains(ability.getName())) {
				continue;
			}

			abilitiesSent.add(ability.getName());
		}
		
		this.iterateAbilities(sender, abilitiesSent);

		//Display the number of Passives and Combos
		final List<String> comboList = this.correctCombos(sender, element);
		if (!comboList.isEmpty()) {
			sender.sendMessage("");
			final ComponentBuilder combos = new ComponentBuilder().appendLegacy(subColor + "Combos (#)".replace("#", String.valueOf(comboList.size())));
			combos.event(hoverEvent(mainColor + this.color(this.hoverType.replace("{type}", subColor + elementName + "Combos"))));
			combos.event(clickEvent("/bending display " + elementName + "Combos"));
			sender.spigot().sendMessage(combos.create());
		}
		
		final List<String> passiveList = this.filterNames(sender, PassiveManager.getPassivesForElement(element));
		if (!passiveList.isEmpty()) {
			if (comboList.isEmpty()) {
				sender.sendMessage("");
			}
			
			final ComponentBuilder passives = new ComponentBuilder().appendLegacy(subColor + "Passives (#)".replace("#", String.valueOf(passiveList.size())));
			passives.event(hoverEvent(mainColor + this.color(this.hoverType.replace("{type}", subColor + elementName + "Passives"))));
			passives.event(clickEvent("/bending display " + elementName + "Passives"));
			sender.spigot().sendMessage(passives.create());
		}

		// Display the subelements and the number of their abilities
		if (Element.getSubElements(element).length > 0) {
			sender.sendMessage("");
			final ComponentBuilder message = new ComponentBuilder();
			for (final SubElement sub : Element.getSubElements(element)) {
				if (sender.hasPermission("bending." + elementName.toLowerCase() + "." + sub.getName().toLowerCase())) {
					final ChatColor color = sub.getColor();
					final String name = sub.getName();
					
					if (!message.getParts().isEmpty()) {
						message.appendLegacy(this.color(this.separator));
					}
					
					message.appendLegacy(color + name + " (#)".replace("#", String.valueOf(filterAbilities(sender, CoreAbility.getAbilitiesByElement(sub)).size())));
					message.event(hoverEvent(color + this.color(this.hoverType.replace("{type}", color + name))));
					message.event(clickEvent("/bending display " + name));
				}
			}
			sender.spigot().sendMessage(message.create());
		}
	}

	/**
	 * Displays a Player's bound abilities.
	 *
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(final CommandSender sender) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		final HashMap<Integer, String> abilities = bPlayer.getAbilities();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + this.noBinds);
			return;
		}

		sender.sendMessage(ChatColor.WHITE + (ChatColor.BOLD + "Abilities"));

		for (int i = 1; i <= 9; i++) {
			final String ability = abilities.get(i);
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil != null && !ability.equalsIgnoreCase("null")) {
				sender.sendMessage(i + ". " + coreAbil.getElement().getColor() + ability);
			}
		}
	}

	private List<String> filterNames(final CommandSender sender, Collection<String> abilities) {
		final List<String> filtered = new ArrayList<>();
		for (String ability : abilities) {
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (filtered.contains(ability) || (sender instanceof Player && !sender.hasPermission("bending.ability." + ability)) || coreAbil == null || (! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility())) {
				continue;
			}
			filtered.add(ability);
		}
		return filtered;
	}

	private Collection<CoreAbility> filterAbilities(final CommandSender sender, Collection<CoreAbility> abilities) {
		final Map<String, CoreAbility> filtered = new LinkedHashMap<>();
		for (CoreAbility coreAbil : abilities) {
			if (filtered.containsValue(coreAbil) || filtered.containsKey(coreAbil.getName()) || (sender instanceof Player && ! sender.hasPermission("bending.ability." + coreAbil.getName())) || ! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility()) {
				continue;
			}
			filtered.put(coreAbil.getName(), coreAbil);
		}
		return filtered.values();
	}

	private ArrayList<String> correctCombos(final CommandSender sender, final Element element) {
		final ArrayList<String> combos = new ArrayList<>();
		if (element instanceof SubElement) {
			for (CoreAbility coreAbil : CoreAbility.getAbilitiesByElement(element)) {
				if (combos.contains(coreAbil.getName()) || (sender instanceof Player && ! sender.hasPermission("bending.ability." + coreAbil.getName())) || coreAbil.isHiddenAbility() || !(coreAbil instanceof ComboAbility)) {
					continue;
				}
				combos.add(coreAbil.getName());
			}
			return combos;
		} else {
			return ComboManager.getCombosForElement(element);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (!args.isEmpty() || !sender.hasPermission("bending.command.display")) {
			return new ArrayList<>();
		}
		final List<String> list = new ArrayList<>();
		list.add("Air");
		list.add("Earth");
		list.add("Fire");
		list.add("Water");
		list.add("Chi");

		for (final Element e : Element.getAddonElements()) {
			list.add(e.getName());
		}

		list.add("Bloodbending");
		list.add("Combustion");
		list.add("Flight");
		list.add("Healing");
		list.add("Ice");
		list.add("Lava");
		list.add("Lightning");
		list.add("Metal");
		list.add("Plantbending");
		list.add("Sand");
		list.add("Spiritual");
		list.add("BlueFire");

		for (final SubElement se : Element.getAddonSubElements()) {
			list.add(se.getName());
		}

		list.add("AirCombos");
		list.add("EarthCombos");
		list.add("FireCombos");
		list.add("WaterCombos");
		list.add("ChiCombos");
		list.add("Avatar");

		list.add("AirPassives");
		list.add("EarthPassives");
		list.add("FirePassives");
		list.add("WaterPassives");
		list.add("ChiPassives");

		return list;
	}
	
	private void fillAbbreviations() {
		abbreviations.put("ac", "aircombo");
		abbreviations.put("ap", "airpassive");
		abbreviations.put("avc", "avatarcombo");
		abbreviations.put("avp", "avatarpassive");
		abbreviations.put("cc", "chicombo");
		abbreviations.put("cp", "chipassive");
		abbreviations.put("ec", "earthcombo");
		abbreviations.put("ep", "earthpassive");
		abbreviations.put("fc", "firecombo");
		abbreviations.put("fp", "firepassive");
		abbreviations.put("wc", "watercombo");
		abbreviations.put("wp", "waterpassive");
	}
	
	private HoverEvent hoverEvent(String string) {
		return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(string));
	}
	
	private ClickEvent clickEvent(String string) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, string);
	}
	
	private Text text(String string) {
		return new Text(new ComponentBuilder().appendLegacy(color(string)).create());
	}
	
	private String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}