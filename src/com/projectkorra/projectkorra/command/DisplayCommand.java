package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Executor for /bending display. Extends {@link PKCommand}.
 */
public class DisplayCommand extends PKCommand {

	public DisplayCommand() {
		super("display", "/bending display <Element>", "This command will show you all of the elements you have bound if you do not specify an element. If you do specify an element (Air, Water, Earth, Fire, or Chi), it will show you all of the available abilities of that element installed on the server.", new String[] { "display", "dis", "d" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) {
			return;
		}

		//bending display [Element]
		if (args.size() == 1) {
			String elementName = args.get(0).toLowerCase().replace("bending", "");
			Element element = Element.fromString(elementName.replace("combos", "").replace("combo", ""));
			//combos
			if (element != null && elementName.contains("combo")) {
				ChatColor color = element != null ? element.getColor() : null;
				ArrayList<String> combos = ComboManager.getCombosForElement(element);

				if (combos.isEmpty()) {
					sender.sendMessage(color + "There are no " + element.getName() + " combos available.");
					return;
				}
				for (String comboMove : combos) {
					ChatColor comboColor = color;
					if (!sender.hasPermission("bending.ability." + comboMove)) {
						continue;
					}

					CoreAbility coreAbil = CoreAbility.getAbility(comboMove);
					if (coreAbil != null) {
						comboColor = coreAbil.getElement().getColor();
					}
					sender.sendMessage(comboColor + comboMove);
				}
				return;
			}
			else if (element != null) {
				if (!element.equals(Element.AVATAR)) {
					if (!(element instanceof SubElement)) {
						displayElement(sender, element);
					} else {
						displaySubElement(sender, element);
					}
				} else {
					displayAvatar(sender);
				}
			}

			else {
				StringBuilder elements = new StringBuilder(ChatColor.RED + "Not a valid argument.");
				elements.append(ChatColor.WHITE + "\nElements: ");
				for (Element e : Element.getAllElements()) {
					if (!(e instanceof SubElement)) {
						elements.append(e.getColor() + e.getName() + ChatColor.WHITE + " | ");
					}
				}
				sender.sendMessage(elements.toString());
				StringBuilder subelements = new StringBuilder(ChatColor.WHITE + "SubElements: ");
				for (SubElement e : Element.getAllSubElements()) {
					subelements.append(ChatColor.WHITE + "\n- " + e.getColor() + e.getName());
				}
				sender.sendMessage(subelements.toString());
			}
		}
		if (args.size() == 0) {
			//bending display
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only usable by players.");
				return;
			}
			displayBinds(sender);
		}
	}

	private void displayAvatar(CommandSender sender) {
		List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(Element.AVATAR);
		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + "There are no " + Element.AVATAR.getColor() + "avatar" + ChatColor.YELLOW + " abilities on this server!");
			return;
		}
		for (CoreAbility ability : abilities) {
			if (ability.isHiddenAbility()) {
				continue;
			}
			if (sender instanceof Player) {
				if (GeneralMethods.canView((Player) sender, ability.getName())) {
					sender.sendMessage(ability.getElement().getColor() + ability.getName());
				}
			} else {
				sender.sendMessage(ability.getElement().getColor() + ability.getName());
			}
		} 
	}

	/**
	 * Displays the enabled moves for the given element to the CommandSender.
	 * 
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(CommandSender sender, Element element) {
		List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(element);

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You must select a valid element.");
			return;
		} else if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.YELLOW + "There are no " + element + " abilities enabled on the server.");
		}

		for (CoreAbility ability : abilities) {
			if (ability instanceof SubAbility || ability.isHiddenAbility()) {
				continue;
			}
			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				sender.sendMessage(ability.getElement().getColor() + ability.getName());
			}
		}

		if (element.equals(Element.CHI)) {
			sender.sendMessage(ChatColor.GOLD + "Combos: " + ChatColor.YELLOW + "/bending display ChiCombos");
		} else {
			sender.sendMessage(element.getSubColor() + "Combos: " + element.getColor() + "/bending display " + element.getName() + "Combos");
			for (SubElement sub : Element.getSubElements(element)) {
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
	private void displaySubElement(CommandSender sender, Element element) {
		List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(element);

		if (abilities.isEmpty() && element != null) {
			sender.sendMessage(ChatColor.YELLOW + "There are no " + element.getColor() + element + ChatColor.YELLOW + " abilities installed!");
			return;
		}
		for (CoreAbility ability : abilities) {
			if (ability.isHiddenAbility()) {
				continue;
			} else if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				sender.sendMessage(element.getColor() + ability.getName());
			}
		}
	}

	/**
	 * Displays a Player's bound abilities.
	 * 
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(CommandSender sender) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
			bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		}
		HashMap<Integer, String> abilities = bPlayer.getAbilities();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You don't have any bound abilities.");
			sender.sendMessage("If you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.");
			return;
		}

		for (int i = 1; i <= 9; i++) {
			String ability = abilities.get(i);
			CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil != null && !ability.equalsIgnoreCase("null"))
				sender.sendMessage(i + " - " + coreAbil.getElement().getColor() + ability);
		}
	}
}
