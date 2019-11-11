package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.ability.api.AddonAbilityInfo;
import com.projectkorra.projectkorra.ability.api.ComboAbilityInfo;
import com.projectkorra.projectkorra.ability.api.PassiveAbilityInfo;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.legacy.SubAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.DisplayCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;

/**
 * Executor for /bending display. Extends {@link PKCommand}.
 */
@SuppressWarnings("rawtypes")
public class DisplayCommand extends PKCommand<DisplayCommandConfig> {

	private final String noCombosAvailable;
	private final String noPassivesAvailable;
	private final String invalidArgument;
	private final String playersOnly;
	private final String noAbilitiesAvailable;
	private final String noBinds;

	public DisplayCommand(final DisplayCommandConfig config) {
		super(config, "display", "/bending display <Element>", config.Description, new String[] { "display", "dis", "d" });

		this.noCombosAvailable = config.NoCombosAvailable;
		this.noPassivesAvailable = config.NoPassivesAvailable;
		this.noAbilitiesAvailable = config.NoAbilitiesAvailable;
		this.invalidArgument = config.InvalidArgument;
		this.playersOnly = ConfigManager.getConfig(CommandPropertiesConfig.class).MustBePlayer;
		this.noBinds = config.NoBinds;
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

			final Element element = this.elementManager.getElement(elementName.replace("combos", "").replace("combo", "").replace("passives", "").replace("passive", ""));
			// combos.
			if (elementName.contains("combo")) {
				if (element == null) {
					sender.sendMessage(ChatColor.BOLD + "Combos");

					for (final Element e : this.elementManager.getElements()) {
						final List<ComboAbilityInfo> abilities = this.comboAbilityManager.getAbilities(e);

						for (final ComboAbilityInfo comboAbilityInfo : abilities) {
							if (!sender.hasPermission("bending.ability." + comboAbilityInfo.getName())) {
								continue;
							}

							String message = comboAbilityInfo.getElement().getColor() + comboAbilityInfo.getName();

							if (comboAbilityInfo instanceof AddonAbilityInfo) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				} else {
					final ChatColor color = element != null ? element.getColor() : null;
					final List<ComboAbilityInfo> abilities = this.comboAbilityManager.getAbilities(element);

					if (abilities.isEmpty()) {
						GeneralMethods.sendBrandingMessage(sender, color + this.noCombosAvailable.replace("{element}", element.getName()));
						return;
					}

					sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Combos"));

					for (final ComboAbilityInfo comboAbilityInfo : abilities) {
						if (!sender.hasPermission("bending.ability." + comboAbilityInfo.getName())) {
							continue;
						}

						String message = comboAbilityInfo.getElement().getColor() + comboAbilityInfo.getName();

						if (comboAbilityInfo instanceof AddonAbilityInfo) {
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

					for (final Element e : this.elementManager.getElements()) {
						final List<PassiveAbilityInfo> passives = this.passiveAbilityManager.getPassives(e);

						for (final PassiveAbilityInfo passiveAbilityInfo : passives) {
							if (!sender.hasPermission("bending.ability." + passiveAbilityInfo.getName())) {
								continue;
							}

							String message = passiveAbilityInfo.getElement().getColor() + passiveAbilityInfo.getName();

							if (passiveAbilityInfo instanceof AddonAbilityInfo) {
								message += ChatColor.WHITE + (ChatColor.BOLD + "*");
							}

							sender.sendMessage(message);
						}
					}
					return;
				}
				final ChatColor color = element != null ? element.getColor() : null;
				final List<PassiveAbilityInfo> passives = this.passiveAbilityManager.getPassives(element);

				if (passives.isEmpty()) {
					GeneralMethods.sendBrandingMessage(sender, color + this.noPassivesAvailable.replace("{element}", element.getName()));
					return;
				}

				sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending() + ChatColor.WHITE + (ChatColor.BOLD + " Passives"));

				for (final PassiveAbilityInfo passiveAbilityInfo : passives) {
					if (!sender.hasPermission("bending.ability." + passiveAbilityInfo.getName())) {
						continue;
					}

					sender.sendMessage(passiveAbilityInfo.getElement().getColor() + passiveAbilityInfo.getName());
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
				for (final Element e : this.elementManager.getElements()) {
					if (!(e instanceof SubElement)) {
						elements.append(e.getColor() + e.getName() + ChatColor.WHITE + " | ");
					}
				}
				sender.sendMessage(elements.toString());
				final StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (final SubElement e : this.elementManager.getSubElements()) {
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
			this.displayBinds((Player) sender);
		}
	}

	/**
	 * Displays the enabled moves for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final List<AbilityInfo> abilities = this.abilityManager.getAbilities(element);

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>(); // Some abilities have the same name. This prevents this from showing anything.
		for (final AbilityInfo abilityInfo : abilities) {
			if (abilityInfo instanceof SubAbility || abilityInfo instanceof ComboAbilityInfo || abilityInfo.isHidden() || abilitiesSent.contains(abilityInfo.getName())) {
				continue;
			}

			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, abilityInfo.getName())) {
				String message = abilityInfo.getElement().getColor() + abilityInfo.getName();
				if (abilityInfo instanceof AddonAbilityInfo) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(abilityInfo.getName());
			}
		}

		if (element.equals(this.elementManager.getChi())) {
			sender.sendMessage(ChatColor.YELLOW + "Combos: " + ChatColor.GOLD + "/bending display ChiCombos");
			sender.sendMessage(ChatColor.YELLOW + "Passives: " + ChatColor.GOLD + "/bending display ChiPassives");
		} else {
			sender.sendMessage(element.getSecondaryColor() + "Combos: " + element.getColor() + "/bending display " + element.getName() + "Combos");
			sender.sendMessage(element.getSecondaryColor() + "Passives: " + element.getColor() + "/bending display " + element.getName() + "Passives");
			for (final SubElement sub : this.elementManager.getSubElements(element)) {
				if (sender.hasPermission("bending." + element.getName().toLowerCase() + "." + sub.getName().toLowerCase())) {
					sender.sendMessage(sub.getColor() + sub.getName() + " abilities: " + element.getColor() + "/bending display " + sub.getName());
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
		final List<AbilityInfo> abilities = this.abilityManager.getAbilities(element);;

		if (abilities.isEmpty() && element != null) {
			sender.sendMessage(ChatColor.YELLOW + this.noAbilitiesAvailable.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW));
			return;
		}

		sender.sendMessage(element.getColor() + (ChatColor.BOLD + element.getName()) + element.getType().getBending());

		final HashSet<String> abilitiesSent = new HashSet<String>();
		for (final AbilityInfo abilityInfo : abilities) {
			if (abilityInfo.isHidden() || abilitiesSent.contains(abilityInfo.getName())) {
				continue;
			} else if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, abilityInfo.getName())) {
				String message = element.getColor() + abilityInfo.getName();
				if (abilityInfo instanceof AddonAbilityInfo) {
					message += ChatColor.WHITE + (ChatColor.BOLD + "*");
				}

				sender.sendMessage(message);
				abilitiesSent.add(abilityInfo.getName());
			}
		}
		sender.sendMessage(element.getParent().getColor() + "Passives: " + element.getColor() + "/bending display " + element.getName() + "Passives");
	}

	/**
	 * Displays a Player's bound abilities.
	 *
	 * @param player The Player to output the bound abilities to
	 */
	private void displayBinds(final Player player) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);
		List<String> abilities = bendingPlayer.getAbilities();

		if (abilities.stream().allMatch(Objects::isNull)) {
			player.sendMessage(ChatColor.RED + this.noBinds);
			return;
		}

		player.sendMessage(ChatColor.WHITE + (ChatColor.BOLD + "Abilities"));

		for (int i = 0; i < 9; i++) {
			final String abilityName = abilities.get(i);
			final AbilityInfo abilityInfo = this.abilityManager.getAbilityInfo(abilityName);

			if (abilityInfo == null) {
				continue;
			}

			String message = (i + 1) + ". " + abilityInfo.getElement().getColor() + abilityName;

			if (abilityInfo instanceof AddonAbilityInfo) {
				message += ChatColor.WHITE + (ChatColor.BOLD + "*");
			}

			player.sendMessage(message);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.display")) {
			return new ArrayList<>();
		}
		final List<String> list = new ArrayList<String>();
		for (Element e : this.elementManager.getAllElements()) {
			list.add(e.getName());
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
