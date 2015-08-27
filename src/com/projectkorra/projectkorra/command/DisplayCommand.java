package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
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
			String element = args.get(0);
			//combos
			if (Arrays.asList(Commands.comboaliases).contains(element)) {
				element = getElement(element);
				Element e = Element.getType(element);
				ArrayList<String> combos = ComboManager.getCombosForElement(e);
				if (combos.isEmpty()) {
					sender.sendMessage(GeneralMethods.getElementColor(e) + "There are no " + element + " combos avaliable.");
					return;
				}
				for (String combomove : combos) {
					if (!sender.hasPermission("bending.ability." + combomove))
						continue;
					ChatColor color = GeneralMethods.getComboColor(combomove);
					sender.sendMessage(color + combomove);
				}
				return;
			}

			//normal elements
			else if (Arrays.asList(Commands.elementaliases).contains(element)) {
				element = getElement(element);
				displayElement(sender, element);
			}

			//subelements
			else if (Arrays.asList(Commands.subelementaliases).contains(element)) {
				displaySubElement(sender, element);
			}

			else {
				sender.sendMessage(ChatColor.RED + "Not a valid Element." + ChatColor.WHITE + " Elements: " + AirMethods.getAirColor() + "Air" + ChatColor.WHITE + " | " + WaterMethods.getWaterColor() + "Water" + ChatColor.WHITE + " | " + EarthMethods.getEarthColor() + "Earth" + ChatColor.WHITE + " | " + FireMethods.getFireColor() + "Fire" + ChatColor.WHITE + " | " + ChiMethods.getChiColor() + "Chi");
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

	/**
	 * Displays the enabled moves for the given element to the CommandSender.
	 * 
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(CommandSender sender, String element) {
		List<String> abilities = ProjectKorra.plugin.abManager.getAbilities(element);
		if (abilities == null) {
			sender.sendMessage(ChatColor.RED + "You must select a valid element.");
			return;
		} else if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "There are no " + element + " abilities enabled on the server.");
		}
		for (String ability : abilities) {
			if (GeneralMethods.isSubAbility(ability))
				continue;
			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability)) {
				sender.sendMessage(GeneralMethods.getElementColor(Element.getType(element)) + ability);
			}
		}
	}

	/**
	 * Displays the enabled moves for the given subelement to the CommandSender.
	 * 
	 * @param sender The CommandSender to show the moves to
	 * @param element The subelement to show the moves for
	 */
	private void displaySubElement(CommandSender sender, String element) {
		List<String> abilities = ProjectKorra.plugin.abManager.getAbilities(element);
		for (String ability : abilities) {
			if (!(sender instanceof Player) || GeneralMethods.canView((Player) sender, ability)) {
				sender.sendMessage(GeneralMethods.getSubBendingColor(Element.getType(getElement(getElement(element)))) + ability);
			}
		}
	}

	/**
	 * Displays a Player's bound abilities.
	 * 
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(CommandSender sender) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(sender.getName());
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
			bPlayer = GeneralMethods.getBendingPlayer(sender.getName());
		}
		HashMap<Integer, String> abilities = bPlayer.getAbilities();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "You don't have any bound abilities.");
			sender.sendMessage("If you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.");
			return;
		}

		for (int i = 1; i <= 9; i++) {
			String ability = abilities.get(i);
			if (ability != null && !ability.equalsIgnoreCase("null"))
				sender.sendMessage(i + " - " + GeneralMethods.getAbilityColor(ability) + ability);
		}
	}
}
