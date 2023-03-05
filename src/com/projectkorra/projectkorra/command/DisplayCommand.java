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
import net.md_5.bungee.api.chat.TextComponent;
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
	private final String subHeader;
	private final String comboPassiveHeader;

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
		this.separator = ConfigManager.languageConfig.get().getString("Commands.Display.Separator").replaceAll("\\\\n", "\n");
		this.hoverType = ConfigManager.languageConfig.get().getString("Commands.Display.HoverType");
		this.hoverAbility = ConfigManager.languageConfig.get().getString("Commands.Display.HoverAbility");
		this.subHeader = ConfigManager.languageConfig.get().getString("Commands.Display.SubHeader").replaceAll("\\\\n", "\n");
		this.comboPassiveHeader = ConfigManager.languageConfig.get().getString("Commands.Display.ComboPassiveHeader").replaceAll("\\\\n", "\n");
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
					final List<String> allCombos = new ArrayList<>();
					final List<String> allPermittedCombos = new ArrayList<>();
					for (final Element e : Element.getAllElements()) {
						final List<String> combos = this.getCombos(e);
						final List<String> permittedCombos = this.filterAbilities(sender, combos);
						allPermittedCombos.addAll(permittedCombos);
						allCombos.addAll(combos);
					}
					
					if (allPermittedCombos.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.GOLD + (allCombos.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", ""));
						return;
					}
					
					sender.sendMessage(ChatColor.BOLD + "Combos");
					this.iterateAbilities(sender, allPermittedCombos);
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
					final List<String> allPassives = new ArrayList<>();
					final List<String> allPermittedPassives = new ArrayList<>();
					for (final Element e : Element.getAllElements()) {
						final Set<String> passives = PassiveManager.getPassivesForElement(e);
						final List<String> permittedPassives = this.filterAbilities(sender, passives);
						allPermittedPassives.addAll(permittedPassives);
						allPassives.addAll(passives);
					}
					
					if (allPermittedPassives.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.GOLD + (allPassives.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", ""));
						return;
					}
					
					sender.sendMessage(ChatColor.BOLD + "Passives");
					this.iterateAbilities(sender, allPermittedPassives);
					return;
				}
				
				final Set<String> allPassives = PassiveManager.getPassivesForElement(element);
				final List<String> passives = this.filterAbilities(sender, allPassives);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + (allPassives.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + " Passives");
				this.iterateAbilities(sender, passives);
				return;
			} else if (element != null) {
				this.displayElement(sender, element);
			} else {
				final ComponentBuilder elements = new ComponentBuilder().appendLegacy(ChatColor.RED + this.invalidArgument).append("\nElements: ").color(ChatColor.WHITE);
				
				for (final Element e : Element.getAllElements()) {
					if (e instanceof SubElement || (sender instanceof Player && !sender.hasPermission("bending." + e.getName()))) {
						continue;
					}
					
					if (elements.getParts().size() > 2) {
						elements.append(" | ").color(ChatColor.WHITE);
					}
					
					elements.appendLegacy(e.getName()).color(e.getColor());
					elements.event(this.hoverEvent(e.getColor() + this.hoverType.replace("{type}", e.getName())));
					elements.event(this.clickEvent("/bending display " + e.getName()));
				}
				
				final ComponentBuilder subelements = new ComponentBuilder().append("SubElements: ").color(ChatColor.WHITE);
				for (final SubElement e : Element.getAllSubElements()) {
					if (sender instanceof Player && !sender.hasPermission("bending." + e.getParentElement().getName() + "." + e.getName())) {
						continue;
					}
					
					if (subelements.getParts().size() > 1) {
						subelements.append(" | ").color(ChatColor.WHITE);
					}
					
					subelements.append(e.getName()).color(e.getColor());
					subelements.event(this.hoverEvent(e.getColor() + this.hoverType.replace("{type}", e.getName())));
					subelements.event(this.clickEvent("/bending display " + e.getName()));
				}
				
				sender.spigot().sendMessage(elements.create());
				sender.spigot().sendMessage(subelements.create());
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
				messageBuilder.appendLegacy(ChatUtil.color(this.separator));
			}

			final ChatColor color = coreAbil.getElement().getColor();
			final ChatColor subColor = coreAbil.getElement() instanceof SubElement ? color : coreAbil.getElement().getSubColor();
			
			messageBuilder.appendLegacy(ChatUtil.color(this.format.replace("{ability}", color + abilityName)));
			messageBuilder.event(this.hoverEvent(color + ChatUtil.color(this.hoverAbility.replace("{ability}", subColor + abilityName))));
			messageBuilder.event(this.clickEvent("/bending help " + coreAbil.getName()));
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

		if (!(combos.isEmpty() || passives.isEmpty()) && ChatColor.stripColor(this.comboPassiveHeader).length() > 0) {
			sender.spigot().sendMessage(TextComponent.fromLegacyText(ChatUtil.multiline(mainColor + ChatUtil.color(this.comboPassiveHeader
					.replaceAll("\\{(?i)element}", elementName).replaceAll("\\{(?i)element_?color}", mainColor.toString())))));
		}

		//Display the number of Passives and Combos
		if (!combos.isEmpty()) {
			final ComponentBuilder messageBuilder = new ComponentBuilder().appendLegacy(subColor + elementName + " Combos (#)".replace("#", String.valueOf(combos.size())));
			messageBuilder.event(this.hoverEvent(mainColor + ChatUtil.color(this.hoverType.replace("{type}", subColor + elementName + "Combos"))));
			messageBuilder.event(this.clickEvent("/bending display " + elementName + "Combos"));
			sender.spigot().sendMessage(messageBuilder.create());
		}
		
		if (!passives.isEmpty()) {
			final ComponentBuilder messageBuilder = new ComponentBuilder().appendLegacy(subColor + elementName + " Passives (#)".replace("#", String.valueOf(passives.size())));
			messageBuilder.event(this.hoverEvent(mainColor + ChatUtil.color(this.hoverType.replace("{type}", subColor + elementName + "Passives"))));
			messageBuilder.event(this.clickEvent("/bending display " + elementName + "Passives"));
			sender.spigot().sendMessage(messageBuilder.create());
		}

		// Display the subelements and the number of their abilities
		if (Element.getSubElements(element).length > 0) {
			final ComponentBuilder message = new ComponentBuilder();
			for (final SubElement sub : Element.getSubElements(element)) {
				final int count = this.filterAbilities(sender, this.getAbilities(sub)).size();
				if ((!(sender instanceof Player) || sender.hasPermission("bending." + elementName.toLowerCase() + "." + sub.getName().toLowerCase())) && count > 0) {
					final ChatColor color = sub.getColor();
					final String name = sub.getName();
					
					if (!message.getParts().isEmpty()) {
						message.appendLegacy(ChatUtil.color(this.separator));
					}
					
					message.appendLegacy(color + name + " Abilities (#)".replace("#", String.valueOf(count)));
					message.event(this.hoverEvent(color + ChatUtil.color(this.hoverType.replace("{type}", color + name))));
					message.event(this.clickEvent("/bending display " + name));
				}
			}
			if (!message.getParts().isEmpty()) {
				if (ChatColor.stripColor(this.subHeader).length() > 0) {
					sender.spigot().sendMessage(TextComponent.fromLegacyText(ChatUtil.multiline(mainColor + ChatUtil.color(this.subHeader)
							.replaceAll("\\{(?i)element}", elementName).replaceAll("\\{(?i)element_?color}", mainColor.toString()))));
				}
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
			if (coreAbil == null || filteredAbilities.contains(coreAbil.getName()) || !coreAbil.isEnabled() || (!(coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility()) || (sender instanceof Player && !sender.hasPermission("bending.ability." + coreAbil.getName()))) {
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
		
		for (final Element e : Element.getAllElements()) {
			if (filterAbilities(sender, getAbilities(e)).size() > 0) {
				list.add(e.getName());
			}
			for (final SubElement s : Element.getSubElements(e)) {
				if (filterAbilities(sender, getAbilities(s)).size() > 0) {
					list.add(s.getName());
				}
			}
		}

		for (String elementName : new ArrayList<>(list)) {
			if (filterAbilities(sender, getCombos(Element.getElement(elementName))).size() > 0)
				list.add(elementName + "Combos");
			if (filterAbilities(sender, PassiveManager.getPassivesForElement(Element.getElement(elementName))).size() > 0)
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
	
	public void addAbbreviation(String abbreviated, String unAbbreviated) {
		abbreviations.put(abbreviated, unAbbreviated);
	}
	
	private HoverEvent hoverEvent(String string) {
		return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text(string));
	}
	
	private ClickEvent clickEvent(String string) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, string);
	}
	
	private Text text(String string) {
		return new Text(new ComponentBuilder().appendLegacy(ChatUtil.color(string)).create());
	}
}