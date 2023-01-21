package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
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
	private final String noCombosAccess;
	private final String noPassivesAccess;
	private final String noAbilitiesAccess;
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
		this.noCombosAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAccess");
		this.noPassivesAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAccess");
		this.noAbilitiesAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAccess");
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
						final List<String> combos = this.filterAbilities(sender, this.getCombos(e));
						this.iterateAbilities(sender, combos);
					}
					return;
				}
				
				final List<String> allCombos = this.getCombos(element);
				final List<String> combos = this.filterAbilities(sender, allCombos);
				
				if (combos.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + (allCombos.isEmpty() ? this.noCombosAvailable : this.noCombosAccess).replace("{element}", element.getName()));
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
				
				final Set<String> allPassives = PassiveManager.getPassivesForElement(element);
				final List<String> passives = this.filterAbilities(sender, allPassives);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + (allPassives.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + " Passives");
				this.iterateAbilities(sender, allPassives);
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
		if (abilityNames.isEmpty()) {
			return;
		}
		
		final ComponentBuilder messageBuilder = new ComponentBuilder();
		for (String abilityName : abilityNames) {
			final CoreAbility coreAbil = CoreAbility.getAbility(abilityName);
			if (coreAbil == null) {
				continue;
			}
			
			if (!messageBuilder.getParts().isEmpty()) {
				messageBuilder.appendLegacy(this.color(this.separator));
			}

			final ChatColor color = coreAbil.getElement().getColor();
			final ChatColor subColor = coreAbil.getElement() instanceof SubElement ? color : coreAbil.getElement().getSubColor();
			
			messageBuilder.appendLegacy(this.color(this.format.replace("{ability}", color + abilityName)));
			messageBuilder.event(hoverEvent(color + this.color(this.hoverAbility.replace("{ability}", subColor + abilityName))));
			messageBuilder.event(clickEvent("/bending help " + coreAbil.getName()));
		}
		sender.spigot().sendMessage(messageBuilder.create());
	}

	/**
	 * Displays the enabled moves and subelements for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final List<String> allAbilities = this.getAbilities(element);
		final List<String> abilities = this.filterAbilities(sender, allAbilities);
		final Set<String> allPassives = PassiveManager.getPassivesForElement(element);
		final List<String> passives = this.filterAbilities(sender, allPassives);
		final List<String> allCombos = this.getCombos(element);
		final List<String> combos = this.filterAbilities(sender, allCombos);
		final String elementName = element.getName();
		final String bending = element.getType().getBending();
		final ChatColor mainColor = element.getColor();
		final ChatColor subColor = element instanceof SubElement ? mainColor : element.getSubColor();

		if (abilities.isEmpty() && combos.isEmpty() && passives.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + (allAbilities.isEmpty() && allCombos.isEmpty() && allPassives.isEmpty() ? this.noAbilitiesAvailable : this.noAbilitiesAccess).replace("{element}", mainColor + elementName + ChatColor.YELLOW));
			return;
		}
		
		sender.sendMessage(mainColor + (ChatColor.BOLD + elementName) + bending);
		
		this.iterateAbilities(sender, abilities);

		//Display the number of Passives and Combos
		if (!combos.isEmpty()) {
			sender.sendMessage("");
			final ComponentBuilder messageBuilder = new ComponentBuilder().appendLegacy(subColor + "Combos (#)".replace("#", String.valueOf(combos.size())));
			messageBuilder.event(hoverEvent(mainColor + this.color(this.hoverType.replace("{type}", subColor + elementName + "Combos"))));
			messageBuilder.event(clickEvent("/bending display " + elementName + "Combos"));
			sender.spigot().sendMessage(messageBuilder.create());
		}
		
		if (!passives.isEmpty()) {
			if (combos.isEmpty()) {
				sender.sendMessage("");
			}
			
			final ComponentBuilder messageBuilder = new ComponentBuilder().appendLegacy(subColor + "Passives (#)".replace("#", String.valueOf(passives.size())));
			messageBuilder.event(hoverEvent(mainColor + this.color(this.hoverType.replace("{type}", subColor + elementName + "Passives"))));
			messageBuilder.event(clickEvent("/bending display " + elementName + "Passives"));
			sender.spigot().sendMessage(messageBuilder.create());
		}

		// Display the subelements and the number of their abilities
		if (Element.getSubElements(element).length > 0) {
			final ComponentBuilder message = new ComponentBuilder();
			for (final SubElement sub : Element.getSubElements(element)) {
				final int count = this.filterAbilities(sender, this.getAbilities(sub)).size() + this.filterAbilities(sender, this.getCombos(sub)).size() + this.filterAbilities(sender, PassiveManager.getPassivesForElement(sub)).size();
				if (sender.hasPermission("bending." + elementName.toLowerCase() + "." + sub.getName().toLowerCase()) && count > 0) {
					final ChatColor color = sub.getColor();
					final String name = sub.getName();
					
					if (!message.getParts().isEmpty()) {
						message.appendLegacy(this.color(this.separator));
					}
					
					message.appendLegacy(color + name + " (#)".replace("#", String.valueOf(count)));
					message.event(hoverEvent(color + this.color(this.hoverType.replace("{type}", color + name))));
					message.event(clickEvent("/bending display " + name));
				}
			}
			if (!message.getParts().isEmpty()) {
				sender.sendMessage("");
				sender.spigot().sendMessage(message.create());
			}
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
	
	private List<String> filterAbilities(final CommandSender sender, final Collection<String> abilities) {
		final List<String> filteredAbilities = new ArrayList<>();
		for (String ability : abilities) {
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null || filteredAbilities.contains(coreAbil.getName()) || !coreAbil.isEnabled() || coreAbil.isHiddenAbility() || (sender instanceof Player && !sender.hasPermission("bending.ability." + coreAbil.getName()))) {
				continue;
			}
			filteredAbilities.add(coreAbil.getName());
		}
		return filteredAbilities;
	}
	
	private List<String> getAbilities(final Element element) {
		final List<String> abilities = new ArrayList<>();
		for (CoreAbility coreAbil : CoreAbility.getAbilitiesByElement(element)) {
			if (!coreAbil.getElement().equals(element) || !coreAbil.isEnabled()|| coreAbil.isHiddenAbility() || abilities.contains(coreAbil.getName()) || coreAbil instanceof ComboAbility || coreAbil instanceof PassiveAbility) {
				continue;
			}
			abilities.add(coreAbil.getName());
		}
		return abilities;
	}
	
	private List<String> getCombos(final Element element) {
		if (element instanceof SubElement) {
			final List<String> combos = new ArrayList<>();
			for (CoreAbility coreAbil : CoreAbility.getAbilitiesByElement(element)) {
				if (!(coreAbil instanceof ComboAbility) || !coreAbil.isEnabled() || coreAbil.isHiddenAbility() || combos.contains(coreAbil.getName())) {
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
		
		for (final Element e : Element.getMainElements()) {
			list.add(e.getName());
			for (final SubElement s : Element.getSubElements(e)) {
				list.add(s.getName());
			}
		}

		for (final Element e : Element.getAddonElements()) {
			list.add(e.getName());
			for (final SubElement s : Element.getAddonSubElements(e)) {
				list.add(s.getName());
			}
		}

		for (String elementName : new ArrayList<>(list)) {
			list.add(elementName + "Combos");
			list.add(elementName + "Passives");
		}

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