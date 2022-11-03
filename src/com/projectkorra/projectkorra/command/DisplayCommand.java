package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
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
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Executor for /bending display. Extends {@link PKCommand}.
 */
public class DisplayCommand extends PKCommand {

	private final String noCombosAvailable;
	private final String noPassivesAvailable;
	private final String invalidArgument;
	private final String playersOnly;
	private final String noAbilitiesAvailable;
	private final String noBinds;
	private final String hoverType;
	private final String hoverAbility;

	private Set<Element> cachedPassiveElements;
	private Set<Element> cachedComboElements;

	public DisplayCommand() {
		super("display", "/bending display <Element>", ConfigManager.languageConfig.get().getString("Commands.Display.Description"), new String[] { "display", "dis", "d" });

		this.noCombosAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAvailable");
		this.noPassivesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAvailable");
		this.noAbilitiesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAvailable");
		this.invalidArgument = ConfigManager.languageConfig.get().getString("Commands.Display.InvalidArgument");
		this.playersOnly = ConfigManager.languageConfig.get().getString("Commands.Display.PlayersOnly");
		this.noBinds = ConfigManager.languageConfig.get().getString("Commands.Display.NoBinds");
		this.hoverType = ConfigManager.languageConfig.get().getString("Commands.Display.HoverType");
		this.hoverAbility = ConfigManager.languageConfig.get().getString("Commands.Display.HoverAbility");

		//1 tick later because commands are created before abilities are
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
			cachedPassiveElements = CoreAbility.getAbilities().stream().filter(PassiveAbility.class::isInstance)
					.filter(Ability::isEnabled).map(Ability::getElement).collect(Collectors.toSet());
			cachedComboElements = CoreAbility.getAbilities().stream().filter(ComboAbility.class::isInstance)
					.filter(ab -> !ab.isHiddenAbility()).filter(Ability::isEnabled).map(Ability::getElement).collect(Collectors.toSet());
		}, 1L);
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}

		// bending display [Element].
		if (args.size() == 1) {
			String elementName = args.get(0).toLowerCase().replace("bending", "");
			if (elementName.equalsIgnoreCase("wc")) {
				elementName = "watercombo";
			} else if (elementName.equalsIgnoreCase("ac")) {
				elementName = "aircombo";
			} else if (elementName.equalsIgnoreCase("ec")) {
				elementName = "earthcombo";
			} else if (elementName.equalsIgnoreCase("fc")) {
				elementName = "firecombo";
			} else if (elementName.equalsIgnoreCase("cc")) {
				elementName = "chicombo";
			} else if (elementName.equalsIgnoreCase("avc")) {
				elementName = "avatarcombo";
			} else if (elementName.equalsIgnoreCase("wp")) {
				elementName = "waterpassive";
			} else if (elementName.equalsIgnoreCase("ap")) {
				elementName = "airpassive";
			} else if (elementName.equalsIgnoreCase("ep")) {
				elementName = "earthpassive";
			} else if (elementName.equalsIgnoreCase("fp")) {
				elementName = "firepassive";
			} else if (elementName.equalsIgnoreCase("cp")) {
				elementName = "chipassive";
			} else if (elementName.equalsIgnoreCase("avp")) {
				elementName = "avatarpassive";
			}
			final Element element = Element.fromString(elementName.replace("combos", "").replace("combo", "").replace("passives", "").replace("passive", ""));
			// combos.
			if (elementName.contains("combo")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Combos");

					for (final Element e : Element.getAllElements()) {
						final ArrayList<String> combos = correctCombos(element);
						iterateAbilities(sender, combos);
					}
				} else {
					final ChatColor color = element.getColor();
					final ArrayList<String> combos = correctCombos(element);

					if (combos.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, color + this.noCombosAvailable.replace("{element}", element.getName()));
						return;
					}

					sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Combos"));
					iterateAbilities(sender, combos);
				}
				return;
				// passives.
			} else if (elementName.contains("passive")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Passives");

					for (final Element e : Element.getAllElements()) {
						final Set<String> passives = PassiveManager.getPassivesForElement(e);
						iterateAbilities(sender, passives);
					}
					return;
				}
				final ChatColor color = element.getColor();
				final Set<String> passives = PassiveManager.getPassivesForElement(element);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, color + this.noPassivesAvailable.replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Passives"));
				iterateAbilities(sender, passives);
				return;
			} else if (element != null) {
				this.displayElement(sender, element);
			} else {
				final StringBuilder elements = new StringBuilder(ChatColor.RED + this.invalidArgument);
				elements.append(ChatColor.WHITE).append("\nElements: ");
				for (final Element e : Element.getAllElements()) {
					if (!(e instanceof SubElement)) {
						elements.append(e.getColor()).append(e.getName()).append(ChatColor.WHITE).append(" | ");
					}
				}
				sender.sendMessage(elements.toString());
				final StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (final SubElement e : Element.getAllSubElements()) {
					subelements.append(ChatColor.WHITE).append("\n- ").append(e.getColor()).append(e.getName());
				}
				sender.sendMessage(subelements.toString());
			}
		}
		if (args.isEmpty()) {
			// bending display.
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
	 * @param iterate The list to iterate through
	 */
	private void iterateAbilities(final CommandSender sender, final Collection<String> iterate) {
		final TextComponent fullMessage = new TextComponent();
		for (String iteration : iterate) {
			final CoreAbility coreAbil = CoreAbility.getAbility(iteration);
			if (sender instanceof Player && !sender.hasPermission("bending.ability." + iteration) || coreAbil == null || (! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility())) {
				continue;
			}

			final ChatColor color = coreAbil.getElement().getColor();
			final ChatColor mainColor = coreAbil.getElement() instanceof SubElement ? ((SubElement) coreAbil.getElement()).getParentElement().getColor(): color;
			final ChatColor subColor = coreAbil.getElement() instanceof SubElement ? ((SubElement) coreAbil.getElement()).getParentElement().getSubColor(): coreAbil.getElement().getSubColor();

			String piece = color + iteration;

			if (coreAbil instanceof AddonAbility) {
				piece += ChatColor.WHITE + (ChatColor.BOLD + "*");
			}
			final TextComponent message = new TextComponent(TextComponent.fromLegacyText(piece));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(mainColor + this.hoverAbility.replace("{ability}", subColor + iteration))));
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bending help " + coreAbil.getName()));
			if (!fullMessage.equals(new TextComponent())) {
				fullMessage.addExtra(", ");
			}
			fullMessage.addExtra(message);
		}
		sender.spigot().sendMessage(fullMessage);
	}

	/**
	 * Displays the enabled moves and subelements for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final List<CoreAbility> abilities = filterAbilities(sender, CoreAbility.getAbilitiesByElement(element));

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW));
			return;
		}

		final ChatColor mainColor = element instanceof SubElement ? ((SubElement) element).getParentElement().getColor() : element.getColor();
		final ChatColor subColor = element instanceof SubElement ? ((SubElement) element).getParentElement().getSubColor() : element.getSubColor();

		sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<>(); // Some abilities have the same name. This prevents this from showing anything.
		for (final CoreAbility ability : abilities) {
			if ((!(element instanceof SubElement) && ability instanceof SubAbility) || ability instanceof PassiveAbility ||  ability instanceof ComboAbility || abilitiesSent.contains(ability.getName())) {
				continue;
			}

			abilitiesSent.add(ability.getName());
		}
		iterateAbilities(sender, abilitiesSent);

		//Display the number of Passives and Combos
		final TextComponent combos = new TextComponent(subColor + "Combos " + "(#c)".replace("#c", String.valueOf(correctCombos(element).size())));
		combos.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(mainColor + this.hoverType.replace("{type}", subColor + element.getName() + "Combos"))));
		combos.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bending display " + element.getName() + "Combos"));

		final TextComponent passives = new TextComponent(", " + subColor + "Passives " + "(#p)".replace("#p", String.valueOf(filterNames(sender, PassiveManager.getPassivesForElement(element)).size())));
		passives.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(mainColor + this.hoverType.replace("{type}", subColor + element.getName() + "Passives"))));
		passives.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bending display " + element.getName() + "Passives"));

		combos.addExtra(passives);
		sender.spigot().sendMessage(combos);

		// Display the subelements and the number of their abilities
		if (Element.getSubElements(element).length > 0) {
			final TextComponent message = new TextComponent("");
			for (final SubElement sub : Element.getSubElements(element)) {
				if (sender.hasPermission("bending." + element.getName().toLowerCase() + "." + sub.getName().toLowerCase())) {
					final TextComponent piece = new TextComponent(TextComponent.fromLegacyText((message.equals(new TextComponent("")) ? subColor + sub.getName() : ", " + subColor + sub.getName()) + " (#)".replace("#", String.valueOf(filterAbilities(sender, CoreAbility.getAbilitiesByElement(sub)).size()))));
					piece.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(mainColor + this.hoverType.replace("{type}", subColor + sub.getName()))));
					piece.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bending display " + sub.getName()));
					message.addExtra(piece);
				}
			}
			sender.spigot().sendMessage(message);
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
				String message = i + ". " + coreAbil.getElement().getColor() + ability;

				if (coreAbil instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
			}
		}
	}

	private List<String> filterNames(final CommandSender sender, Collection<String> abilites) {
		final List<String> filtered = new ArrayList<>();
		for (String ability : abilites) {
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (filtered.contains(ability) || sender instanceof Player && !sender.hasPermission("bending.ability." + ability) || coreAbil == null || (! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility())) {
				continue;
			}
			filtered.add(ability);
		}
		return filtered;
	}

	private List<CoreAbility> filterAbilities(final CommandSender sender, Collection<CoreAbility> abilites) {
		final List<CoreAbility> filtered = new ArrayList<>();
		final List<String> added = new ArrayList<>();
		for (CoreAbility coreAbil : abilites) {
			if (filtered.contains(coreAbil) || added.contains(coreAbil.getName()) || sender instanceof Player && ! sender.hasPermission("bending.ability." + coreAbil.getName()) || ! (coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility()) {
				continue;
			}
			filtered.add(coreAbil);
			added.add(coreAbil.getName());
		}
		return filtered;
	}

	private ArrayList<String> correctCombos(final Element element) {
		final ArrayList<String> combos = new ArrayList<>();
		if (element instanceof SubElement) {
			for (CoreAbility ability : CoreAbility.getAbilitiesByElement(element)) {
				if (ability instanceof ComboAbility) {
					combos.add(ability.getName());
				}
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
}