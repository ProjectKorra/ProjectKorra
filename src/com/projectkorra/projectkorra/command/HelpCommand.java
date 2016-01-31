package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;

/**
 * Executor for /bending help. Extends {@link PKCommand}.
 */
public class HelpCommand extends PKCommand {
	public HelpCommand() {
		super("help", "/bending help [Topic/Page]", "This command provides information on how to use other commands in ProjectKorra.", new String[] { "help", "h" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1))
			return;
		else if (args.size() == 0) {
			List<String> strings = new ArrayList<String>();
			for (PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && sender.hasPermission("bending.command." + command.getName())) {
					strings.add(command.getProperUse());
				}
			}
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);
			for (String s : getPage(strings, ChatColor.GOLD + "Commands: <required> [optional]", 1, false)) {
				sender.sendMessage(ChatColor.YELLOW + s);
			}
			return;
		}

		String arg = args.get(0).toLowerCase();
		if (isNumeric(arg)) {
			List<String> strings = new ArrayList<String>();
			for (PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			for (String s : getPage(strings, ChatColor.GOLD + "Commands: <required> [optional]", Integer.valueOf(arg), true)) {
				sender.sendMessage(ChatColor.YELLOW + s);
			}
		} else if (instances.keySet().contains(arg.toLowerCase())) {//bending help command
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { //bending help elementcombo
			sender.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.RED + "/bending display " + arg + ChatColor.GOLD + " or " + ChatColor.RED + "/bending help <Combo Name>");
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility)) { //bending help ability
			CoreAbility ability = CoreAbility.getAbility(arg);
			ChatColor color = ability.getElement().getColor();
			sender.sendMessage(color + ability.getName() + " - ");
			sender.sendMessage(color + ability.getDescription());
		} else if (Arrays.asList(Commands.airaliases).contains(args.get(0))) {
			sender.sendMessage(Element.AIR.getColor() + "Air is the element of freedom. Airbenders are natural pacifists and " + "great explorers. There is nothing stopping them from scaling the tallest of mountains and walls easily. They specialize in redirection, " + "from blasting things away with gusts of winds, to forming a shield around them to prevent damage. Easy to get across flat terrains, " + "such as oceans, there is practically no terrain off limits to Airbenders. They lack much raw damage output, but make up for it with " + "with their ridiculous amounts of utility and speed.");
			sender.sendMessage(ChatColor.YELLOW + "Airbenders can chain their abilities into combos, type " + Element.AIR.getColor() + "/b help AirCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qffg9m3");
		} else if (Arrays.asList(Commands.wateraliases).contains(args.get(0))) {
			sender.sendMessage(Element.WATER.getColor() + "Water is the element of change. Waterbending focuses on using your " + "opponents own force against them. Using redirection and various dodging tactics, you can be made " + "practically untouchable by an opponent. Waterbending provides agility, along with strong offensive " + "skills while in or near water.");
			sender.sendMessage(ChatColor.YELLOW + "Waterbenders can chain their abilities into combos, type " + Element.WATER.getColor() + "/b help WaterCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/lod3plv");
		} else if (Arrays.asList(Commands.earthaliases).contains(args.get(0))) {
			sender.sendMessage(Element.EARTH.getColor() + "Earth is the element of substance. Earthbenders share many of the " + "same fundamental techniques as Waterbenders, but their domain is quite different and more readily " + "accessible. Earthbenders dominate the ground and subterranean, having abilities to pull columns " + "of rock straight up from the earth or drill their way through the mountain. They can also launch " + "themselves through the air using pillars of rock, and will not hurt themselves assuming they land " + "on something they can bend. The more skilled Earthbenders can even bend metal.");
			//sender.sendMessage(ChatColor.YELLOW + "Earthbenders can chain their abilities into combos, type " + EarthMethods.getEarthColor() + "/b help EarthCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qaudl42");
		} else if (Arrays.asList(Commands.firealiases).contains(args.get(0))) {
			sender.sendMessage(Element.FIRE.getColor() + "Fire is the element of power. Firebenders focus on destruction and " + "incineration. Their abilities are pretty straight forward: set things on fire. They do have a bit " + "of utility however, being able to make themselves un-ignitable, extinguish large areas, cook food " + "in their hands, extinguish large areas, small bursts of flight, and then comes the abilities to shoot " + "fire from your hands.");
			sender.sendMessage(ChatColor.YELLOW + "Firebenders can chain their abilities into combos, type " + Element.FIRE.getColor() + "/b help FireCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/k4fkjhb");
		} else if (Arrays.asList(Commands.chialiases).contains(args.get(0))) {
			sender.sendMessage(Element.CHI.getColor() + "Chiblockers focus on bare handed combat, utilizing their agility and " + "speed to stop any bender right in their path. Although they lack the ability to bend any of the " + "other elements, they are great in combat, and a serious threat to any bender. Chiblocking was " + "first shown to be used by Ty Lee in Avatar: The Last Airbender, then later by members of the " + "Equalists in The Legend of Korra.");
			sender.sendMessage(ChatColor.YELLOW + "Chiblockers can chain their abilities into combos, type " + Element.CHI.getColor() + "/b help ChiCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/mkp9n6y");
		} else {
			//combos - handled differently because they're stored in CamelCase in ComboManager
			for (String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					CoreAbility coreAbility = CoreAbility.getAbility(combo);
					ChatColor color = coreAbility != null ? coreAbility.getElement().getColor() : null;
					sender.sendMessage(color + combo + " (Combo) - ");
					sender.sendMessage(color + ComboManager.getDescriptions().get(combo));
					sender.sendMessage(ChatColor.GOLD + "Usage: " + ComboManager.getInstructions().get(combo));
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "That isn't a valid help topic. Use /bending help for more information.");
		}
	}
}
