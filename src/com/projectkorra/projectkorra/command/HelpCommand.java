package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
				if (!command.getName().equalsIgnoreCase("help")) {
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
		
		String arg = args.get(0);
		
		if (isNumeric(arg)) {
			List<String> strings = new ArrayList<String>();
			for (PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help")) {
					strings.add(command.getProperUse());
				}
			}
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);
			for (String s : getPage(strings, ChatColor.GOLD + "Commands: <required> [optional]", Integer.valueOf(arg), false)) {
				sender.sendMessage(ChatColor.YELLOW + s);
			}
		} else if (instances.keySet().contains(arg.toLowerCase())) {//bending help command
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { //bending help elementcombo
			sender.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.RED + "/bending display " + arg + ChatColor.GOLD + " or " + ChatColor.RED + "/bending help <Combo Name>");
		} else if (GeneralMethods.abilityExists(arg)) { //bending help ability
			String ability = GeneralMethods.getAbility(arg);
			ChatColor color = GeneralMethods.getAbilityColor(ability);
			sender.sendMessage(color + ability + " - ");
			sender.sendMessage(color + AbilityModuleManager.descriptions.get(GeneralMethods.getAbility(ability)));
		} else if (Arrays.asList(Commands.airaliases).contains(args.get(0))) {
			sender.sendMessage(AirMethods.getAirColor() + "Air is the element of freedom. Airbenders are natural pacifists and " + "great explorers. There is nothing stopping them from scaling the tallest of mountains and walls easily. They specialize in redirection, " + "from blasting things away with gusts of winds, to forming a shield around them to prevent damage. Easy to get across flat terrains, " + "such as oceans, there is practically no terrain off limits to Airbenders. They lack much raw damage output, but make up for it with " + "with their ridiculous amounts of utility and speed.");
			sender.sendMessage(ChatColor.YELLOW + "Airbenders can chain their abilities into combos, type " + AirMethods.getAirColor() + "/b help AirCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qffg9m3");
		} else if (Arrays.asList(Commands.wateraliases).contains(args.get(0))) {
			sender.sendMessage(WaterMethods.getWaterColor() + "Water is the element of change. Waterbending focuses on using your " + "opponents own force against them. Using redirection and various dodging tactics, you can be made " + "practically untouchable by an opponent. Waterbending provides agility, along with strong offensive " + "skills while in or near water.");
			sender.sendMessage(ChatColor.YELLOW + "Waterbenders can chain their abilities into combos, type " + WaterMethods.getWaterColor() + "/b help WaterCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/lod3plv");
		} else if (Arrays.asList(Commands.earthaliases).contains(args.get(0))) {
			sender.sendMessage(EarthMethods.getEarthColor() + "Earth is the element of substance. Earthbenders share many of the " + "same fundamental techniques as Waterbenders, but their domain is quite different and more readily " + "accessible. Earthbenders dominate the ground and subterranean, having abilities to pull columns " + "of rock straight up from the earth or drill their way through the mountain. They can also launch " + "themselves through the air using pillars of rock, and will not hurt themselves assuming they land " + "on something they can bend. The more skilled Earthbenders can even bend metal.");
			//sender.sendMessage(ChatColor.YELLOW + "Earthbenders can chain their abilities into combos, type " + EarthMethods.getEarthColor() + "/b help EarthCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qaudl42");
		} else if (Arrays.asList(Commands.firealiases).contains(args.get(0))) {
			sender.sendMessage(FireMethods.getFireColor() + "Fire is the element of power. Firebenders focus on destruction and " + "incineration. Their abilities are pretty straight forward: set things on fire. They do have a bit " + "of utility however, being able to make themselves un-ignitable, extinguish large areas, cook food " + "in their hands, extinguish large areas, small bursts of flight, and then comes the abilities to shoot " + "fire from your hands.");
			sender.sendMessage(ChatColor.YELLOW + "Firebenders can chain their abilities into combos, type " + FireMethods.getFireColor() + "/b help FireCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/k4fkjhb");
		} else if (Arrays.asList(Commands.chialiases).contains(args.get(0))) {
			sender.sendMessage(ChiMethods.getChiColor() + "Chiblockers focus on bare handed combat, utilizing their agility and " + "speed to stop any bender right in their path. Although they lack the ability to bend any of the " + "other elements, they are great in combat, and a serious threat to any bender. Chiblocking was " + "first shown to be used by Ty Lee in Avatar: The Last Airbender, then later by members of the " + "Equalists in The Legend of Korra.");
			sender.sendMessage(ChatColor.YELLOW + "Chiblockers can chain their abilities into combos, type " + ChiMethods.getChiColor() + "/b help ChiCombos" + ChatColor.YELLOW + " for more information.");
			sender.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/mkp9n6y");
		} else {
			//combos - handled differently because they're stored in CamelCase in ComboManager
			for (String combo : ComboManager.descriptions.keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					ChatColor color = GeneralMethods.getComboColor(combo);
					sender.sendMessage(color + combo + " (Combo) - ");
					sender.sendMessage(color + ComboManager.descriptions.get(combo));
					sender.sendMessage(ChatColor.GOLD + "Usage: " + ComboManager.instructions.get(combo));
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "That isn't a valid help topic. Use /bending help for more information.");
		}
	}
}
