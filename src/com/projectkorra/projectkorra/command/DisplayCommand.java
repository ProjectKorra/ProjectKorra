package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;

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
			String elementName = args.get(0).toLowerCase();
			//combos
			if (Arrays.asList(Commands.comboaliases).contains(elementName)) {
				elementName = this.getElement(elementName);
				Element element = Element.getElement(elementName);
				ChatColor color = element != null ? element.getColor() : null;
				ArrayList<String> combos = ComboManager.getCombosForElement(element);
				
				if (combos.isEmpty()) {
					sender.sendMessage(color + "There are no " + elementName + " combos avaliable.");
					return;
				}
				for (String combomove : combos) {
					if (!sender.hasPermission("bending.ability." + combomove))
						continue;
					sender.sendMessage(color + combomove);
				}
				return;
			}

			//normal elements
			else if (Arrays.asList(Commands.elementaliases).contains(elementName)) {
				elementName = getElement(elementName);
				displayElement(sender, elementName);
			}

			//subelements
			else if (Arrays.asList(Commands.subelementaliases).contains(elementName)) {
				displaySubElement(sender, elementName);
			}
			
			//avatar
			else if (Arrays.asList(Commands.avataraliases).contains(elementName)) {
				displayAvatar(sender);
			}

			else {
				ChatColor w = ChatColor.WHITE;
				sender.sendMessage(ChatColor.RED + "Not a valid argument." + ChatColor.WHITE + "\nElements: " 
						+ Element.AIR.getColor() + "Air" + ChatColor.WHITE + " | " 
						+ Element.WATER.getColor() + "Water" + ChatColor.WHITE + " | " 
						+ Element.EARTH.getColor() + "Earth" + ChatColor.WHITE + " | " 
						+ Element.FIRE.getColor() + "Fire" + ChatColor.WHITE + " | " 
						+ Element.CHI.getColor() + "Chi");
				sender.sendMessage(w + "SubElements: "
						+ w + "\n-" + Element.AIR.getColor() + " Flight"
						+ w + "\n-" + Element.EARTH.getColor() + " Lavabending"
						+ w + "\n-" + Element.EARTH.getColor() + " Metalbending"
						+ w + "\n-" + Element.EARTH.getColor() + " Sandbending"
						+ w + "\n-" + Element.FIRE.getColor() + " Combustion"
						+ w + "\n-" + Element.FIRE.getColor() + " Lightning"
						+ w + "\n-" + Element.WATER.getColor() + " Bloodbending"
						+ w + "\n-" + Element.WATER.getColor() + " Healing"
						+ w + "\n-" + Element.WATER.getColor() + " Icebending"
						+ w + "\n-" + Element.WATER.getColor() + " Plantbending");
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
	private void displayElement(CommandSender sender, String element) {
		element = this.getElement(element);
		List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(Element.getElement(element));
		
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
		
		if (element.equalsIgnoreCase("earth")) {
			if (sender.hasPermission("bending.earth.lavabending")) {
				sender.sendMessage(ChatColor.DARK_GREEN + "Lavabending abilities: " + ChatColor.GREEN + "/bending display Lavabending");
			}
			if (sender.hasPermission("bending.earth.metalbending")) {
				sender.sendMessage(ChatColor.DARK_GREEN + "Metalbending abilities: " + ChatColor.GREEN + "/bending display Metalbending");
			}
			if (sender.hasPermission("bending.earth.sandbending")) {
				sender.sendMessage(ChatColor.DARK_GREEN + "Sandbending abilities: " + ChatColor.GREEN + "/bending display Sandbending");
			}
		} else if (element.equalsIgnoreCase("air")) {
			sender.sendMessage(ChatColor.DARK_GRAY + "Combos: " + ChatColor.GRAY + "/bending display AirCombos");
			if (sender.hasPermission("bending.air.flight")) {
				sender.sendMessage(ChatColor.DARK_GRAY + "Flight abilities: " + ChatColor.GRAY + "/bending display Flight");
			}
		} else if (element.equalsIgnoreCase("fire")) {
			sender.sendMessage(ChatColor.DARK_RED + "Combos: " + ChatColor.RED + "/bending display FireCombos");
			if (sender.hasPermission("bending.fire.lightningbending")) {
				sender.sendMessage(ChatColor.DARK_RED + "Lightning abilities: " + ChatColor.RED + "/bending display Lightning");
			}
			if (sender.hasPermission("bending.fire.combustionbending")) {
				sender.sendMessage(ChatColor.DARK_RED + "Combustion abilities: " + ChatColor.RED + "/bending display Combustion");
			}
		} else if (element.equalsIgnoreCase("water")) {
			sender.sendMessage(ChatColor.DARK_AQUA + "Combos: " + ChatColor.AQUA + "/bending display WaterCombos");
			if (sender.hasPermission("bending.water.bloodbending")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Bloodbending abilities: " + ChatColor.AQUA + "/bending display Bloodbending");
			}
			if (sender.hasPermission("bending.water.healing")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Healing abilities: " + ChatColor.AQUA + "/bending display Healing");
			}
			if (sender.hasPermission("bending.water.icebending")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Icebending abilities: " + ChatColor.AQUA + "/bending display Icebending");
			}
			if (sender.hasPermission("bending.water.plantbending")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Plantbending abilities: " + ChatColor.AQUA + "/bending display Plantbending");
			}
		} else if (element.equalsIgnoreCase("chi")) {
			sender.sendMessage(ChatColor.GOLD + "Combos: " + ChatColor.YELLOW + "/bending display ChiCombos");
		}
	}

	/**
	 * Displays the enabled moves for the given subelement to the CommandSender.
	 * 
	 * @param sender The CommandSender to show the moves to
	 * @param element The subelement to show the moves for
	 */
	private void displaySubElement(CommandSender sender, String element) {
		element = this.getElement(element);
		Element mainElement = Element.getElement(element);
		List<CoreAbility> abilities = CoreAbility.getAbilitiesByElement(mainElement);
		
		
		if (mainElement instanceof SubElement) {
			mainElement = ((SubElement) mainElement).getParentElement();
		}
		ChatColor color = mainElement != null ? mainElement.getColor() : null;
		
		if (abilities.isEmpty() && mainElement != null) {
			sender.sendMessage(ChatColor.YELLOW + "There are no " + color + element + ChatColor.YELLOW + " abilities installed!");
			return;
		}
		for (CoreAbility ability : abilities) {
			if (ability.isHiddenAbility()) {
				continue;
			} else if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability.getName())) {
				sender.sendMessage(color + ability.getName());
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
