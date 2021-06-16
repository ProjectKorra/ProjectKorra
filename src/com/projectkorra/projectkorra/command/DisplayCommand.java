package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

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

	public DisplayCommand() {
		super("display", "/bending display <Element>", ConfigManager.languageConfig.get().getString("Commands.Display.Description"), new String[] { "display", "dis", "d" });

		this.noCombosAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAvailable");
		this.noPassivesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAvailable");
		this.noAbilitiesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAvailable");
		this.invalidArgument = ConfigManager.languageConfig.get().getString("Commands.Display.InvalidArgument");
		this.playersOnly = ConfigManager.languageConfig.get().getString("Commands.Display.PlayersOnly");
		this.noBinds = ConfigManager.languageConfig.get().getString("Commands.Display.NoBinds");
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
						final ChatColor color = e != null ? e.getColor() : null;
						final ArrayList<String> combos = ComboManager.getCombosForElement(e);

						for (final String comboAbil : combos) {
							ChatColor comboColor = color;
							if (!sender.hasPermission("bending.ability." + comboAbil)) {
								continue;
							}

							final CoreAbility coreAbil = CoreAbility.getAbility(comboAbil);
							if (coreAbil == null || coreAbil.isHiddenAbility()) {
								continue;
							}
							comboColor = coreAbil.getElement().getColor();

							String message = (comboColor + comboAbil);

							if (coreAbil instanceof AddonAbility) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				} else {
					final ChatColor color = element != null ? element.getColor() : null;
					final ArrayList<String> combos = ComboManager.getCombosForElement(element);

					if (combos.isEmpty()) {
						GeneralMethods.sendBrandingMessage(sender, color + this.noCombosAvailable.replace("{element}", element.getName()));
						return;
					}

					sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Combos"));

					for (final String comboMove : combos) {
						ChatColor comboColor = color;
						if (!sender.hasPermission("bending.ability." + comboMove)) {
							continue;
						}

						final CoreAbility coreAbil = CoreAbility.getAbility(comboMove);
						if (coreAbil == null || coreAbil.isHiddenAbility()) {
							continue;
						}
						comboColor = coreAbil.getElement().getColor();

						String message = (comboColor + comboMove);

						if (coreAbil instanceof AddonAbility) {
							message += ChatColor.WHITE + (ChatColor.BOLD + "*");
						}

						sender.sendMessage(message);
					}
					return;
				}
				// passives.
			} else if (elementName.contains("passive")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Passives");

					for (final Element e : Element.getAllElements()) {
						final ChatColor color = e != null ? e.getColor() : null;
						final Set<String> passives = PassiveManager.getPassivesForElement(e);

						for (final String passiveAbil : passives) {
							ChatColor passiveColor = color;
							if (!sender.hasPermission("bending.ability." + passiveAbil)) {
								continue;
							}

							final CoreAbility coreAbil = CoreAbility.getAbility(passiveAbil);
							if (coreAbil == null) {
								continue;
							}
							passiveColor = coreAbil.getElement().getColor();

							String message = (passiveColor + passiveAbil);

							if (coreAbil instanceof AddonAbility) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				}
				final ChatColor color = element != null ? element.getColor() : null;
				final Set<String> passives = PassiveManager.getPassivesForElement(element);

				if (passives.isEmpty()) {
					GeneralMethods.sendBrandingMessage(sender, color + this.noPassivesAvailable.replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Passives"));

				for (final String passiveAbil : passives) {
					ChatColor passiveColor = color;
					if (!sender.hasPermission("bending.ability." + passiveAbil)) {
						continue;
					}

					final CoreAbility coreAbil = CoreAbility.getAbility(passiveAbil);
					if (coreAbil == null) {
						continue;
					}
					passiveColor = coreAbil.getElement().getColor();

					sender.sendMessage(passiveColor + passiveAbil);
				}
				return;
			} else if (element != null) {
				if (!(element instanceof SubElement)) {
					this.displayElement(sender, element);
				} else {
					this.displaySubElement(sender, (SubElement) element);
				}
			}

			else {
				final StringBuilder elements = new StringBuilder(ChatColor.RED + this.invalidArgument);
				elements.append(ChatColor.WHITE + "\nElements: ");
				for (final Element e : Element.getAllElements()) {
					if (!(e instanceof SubElement)) {
						elements.append(e.getColor() + e.getName() + ChatColor.WHITE + " | ");
					}
				}
				sender.sendMessage(elements.toString());
				final StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (final SubElement e : Element.getAllSubElements()) {
					subelements.append(ChatColor.WHITE + "\n- " + e.getColor() + e.getName());
				}
				sender.sendMessage(subelements.toString());
			}
		}
		if (args.size() == 0) {
			// bending display.
			if (!(sender instanceof Player)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playersOnly);
				return;
			}
			this.displayBinds(sender);
		}
	}

	/**
	 * Displays the enabled moves for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(element);

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>(); // Some abilities have the same name. This prevents this from showing anything.
		for (final CoreAbility ability : abilities) {
			if (ability instanceof SubAbility || ability instanceof ComboAbility || ability.isHiddenAbility() || abilitiesSent.contains(ability.getName())) {
				continue;
			}

			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				String message = ability.getElement().getColor() + ability.getName();
				if (ability instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(ability.getName());
			}
		}

		if (element.equals(Element.CHI)) {
			sender.sendMessage(ChatColor.YELLOW + "Combos: " + ChatColor.GOLD + "/bending display ChiCombos");
			sender.sendMessage(ChatColor.YELLOW + "Passives: " + ChatColor.GOLD + "/bending display ChiPassives");
		} else {
			sender.sendMessage(element.getSubColor() + "Combos: " + element.getColor() + "/bending display " + element.toString() + "Combos");
			sender.sendMessage(element.getSubColor() + "Passives: " + element.getColor() + "/bending display " + element.toString() + "Passives");
			for (final SubElement sub : Element.getSubElements(element)) {
				if (sender.hasPermission("bending." + element.getName().toLowerCase() + "." + sub.getName().toLowerCase())) {
					sender.sendMessage(sub.toString() + " abilities: " + element.getColor() + "/bending display " + sub.toString());
				}
			}
		}
	}

	/**
	 * Displays the enabled moves for the given subelement to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The subelement to show the moves for
	 */
	private void displaySubElement(final CommandSender sender, final SubElement element) {
		final List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(element);

		if (abilities.isEmpty() && element != null) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>();
		for (final CoreAbility ability : abilities) {
			if (ability.isHiddenAbility() || abilitiesSent.contains(ability.getName())) {
				continue;
			} else if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				String message = element.getColor() + ability.getName();
				if (ability instanceof AddonAbility) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(ability.getName());
			}
		}
		sender.sendMessage(element.getParentElement().getColor() + "Passives: " + element.getColor() + "/bending display " + element.getName() + "Passives");
	}

	/**
	 * Displays a Player's bound abilities.
	 *
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(final CommandSender sender) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
			bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		}
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

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.display")) {
			return new ArrayList<String>();
		}
		final List<String> list = new ArrayList<String>();
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
