package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.ability.combo.ComboAbilityModule;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.ability.combo.ComboManager.ComboAbility;
import com.projectkorra.projectkorra.ability.combo.ComboModuleManager;
import com.projectkorra.projectkorra.object.Preset;

/**
 * Completes tabbing for the bending command/subcommands.
 * 
 * @author StrangeOne101
 * */
public class BendingTabComplete implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0 || args[0].equals(""))
			return getPossibleCompletionsForGivenArgs(args, getCommandsForUser(sender));
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("bind") || args[0].equalsIgnoreCase("b")) {
				if (args.length > 3 || !sender.hasPermission("bending.command.bind") || !(sender instanceof Player))
					return new ArrayList<String>();
				List<String> abilities = new ArrayList<String>();
				if (args.length == 2) {
					for (String abil : AbilityModuleManager.abilities) {
						if (GeneralMethods.canBind(sender.getName(), abil)) {
							abilities.add(abil);
						}
					}
				} else {
					for (int i = 1; i < 10; i++) {
						abilities.add("" + i);
					}
				}
				Collections.sort(abilities);
				return getPossibleCompletionsForGivenArgs(args, abilities);
			} else if (args[0].equalsIgnoreCase("display") || args[0].equalsIgnoreCase("d")) {
				if (args.length > 2 || !sender.hasPermission("bending.command.display"))
					return new ArrayList<String>();
				List<String> list = new ArrayList<String>();
				list.add("Air");
				list.add("Earth");
				list.add("Fire");
				list.add("Water");
				list.add("Chi");
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
				list.add("SpiritualProjection");
				list.add("AirCombos");
				list.add("EarthCombos");
				list.add("FireCombos");
				list.add("WaterCombos");
				list.add("ChiCombos");
				list.add("Avatar");
				return getPossibleCompletionsForGivenArgs(args, list);
			} else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("choose") || args[0].equalsIgnoreCase("ch")) {
				if (args.length > 3 || !sender.hasPermission("bending.command.add"))
					return new ArrayList<String>();
				List<String> l = new ArrayList<String>();
				if (args.length == 2)
				{
					l.add("Air");
					l.add("Earth");
					l.add("Fire");
					l.add("Water");
					l.add("Chi");
				}
				else
				{
					for (Player p : Bukkit.getOnlinePlayers()) {
						l.add(p.getName());
					}
				}
				return getPossibleCompletionsForGivenArgs(args, l);
			} else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("cl") || args[0].equalsIgnoreCase("c")) {
				if (args.length > 2 || !sender.hasPermission("bending.command.clear"))
					return new ArrayList<String>();
				List<String> l = new ArrayList<String>();
				for (int i = 1; i < 10; i++) {
					l.add("" + i);
				}
				return l;
			} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
				if (args.length > 2 || !sender.hasPermission("bending.command.help"))
					return new ArrayList<String>();
				List<String> list = new ArrayList<String>();
				for (Element e : Element.values()) {
					list.add(e.toString());
				}
				List<String> abils = new ArrayList<String>();
				for (String abil : AbilityModuleManager.abilities) {
					if (GeneralMethods.canBind(sender.getName(), abil)) {
						abils.add(abil);
					}
				}
				for (ComboAbility abil : ComboManager.comboAbilityList.values()) {
					if (GeneralMethods.canBind(sender.getName(), abil.getName()) && (abil.getName() != "IceBulletRightClick" && abil.getName() != "IceBulletLeftClick")) {
						abils.add(abil.getName());
					}
				}
				for (ComboAbilityModule abil : ComboModuleManager.combo) {
					if (GeneralMethods.canBind(sender.getName(), abil.getName())) {
						abils.add(abil.getName());
					}
				}
				Collections.sort(abils);
				list.addAll(abils);
				return getPossibleCompletionsForGivenArgs(args, list);
			} else if (args[0].equalsIgnoreCase("permaremove") || args[0].equalsIgnoreCase("pr") || args[0].equalsIgnoreCase("premove") || args[0].equalsIgnoreCase("permremove")) {
				if (args.length > 2 || !sender.hasPermission("bending.command.permaremove"))
					return new ArrayList<String>();
				List<String> players = new ArrayList<String>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					players.add(p.getName());
				}
				return getPossibleCompletionsForGivenArgs(args, players);
			} else if (args[0].equalsIgnoreCase("preset") || args[0].equalsIgnoreCase("presets") || args[0].equalsIgnoreCase("pre") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("p")) {
				if (args.length > 3 || !sender.hasPermission("bending.command.preset") || !(sender instanceof Player))
					return new ArrayList<String>();
				List<String> l = new ArrayList<String>();
				if (args.length == 2) {
					l.add("create");
					l.add("delete");
					l.add("list");
					l.add("bind");
					return getPossibleCompletionsForGivenArgs(args, l);
				} else if (args.length == 3 && Arrays.asList(new String[] { "delete", "d", "del", "bind", "b" }).contains(args[1].toLowerCase())) {
					List<Preset> presets = Preset.presets.get(((Player) sender).getUniqueId());
					List<String> presetNames = new ArrayList<String>();
					if (presets != null && presets.size() != 0) {
						for (Preset preset : presets) {
							presetNames.add(preset.getName());
						}
					} else
						return new ArrayList<String>();
					return getPossibleCompletionsForGivenArgs(args, presetNames);
				}
			} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
				if (args.length > 3 || !sender.hasPermission("bending.command.remove"))
					return new ArrayList<String>();
				List<String> l = new ArrayList<String>();
				if (args.length == 2) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						l.add(p.getName());
					}
				} else {
					l.add("Air");
					l.add("Earth");
					l.add("Fire");
					l.add("Water");
					l.add("Chi");
				}
				return getPossibleCompletionsForGivenArgs(args, l);
			} else if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("w")) {
				if (args.length > 2 || !sender.hasPermission("bending.command.who"))
					return new ArrayList<String>();
				List<String> l = new ArrayList<String>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					l.add(p.getName());
				}
				return getPossibleCompletionsForGivenArgs(args, l);
			} else if (!PKCommand.instances.keySet().contains(args[0].toLowerCase())) {
				return new ArrayList<String>();
			}
		} else {
			return getPossibleCompletionsForGivenArgs(args, getCommandsForUser(sender));
		}
		return new ArrayList<String>();
	}

	/**
	 * Breaks down the possible list and returns what is applicble depending on
	 * what the user has currently typed.
	 * 
	 * @author D4rKDeagle<br>
	 * <br>
	 *         (Found at
	 *         <a>https://bukkit.org/threads/help-with-bukkit-tab-completion
	 *         -api.166436</a>)
	 * @param args Args of the command. Provide all of them.
	 * @param possibilitiesOfCompletion List of things that can be given
	 */
	public static List<String> getPossibleCompletionsForGivenArgs(String[] args, List<String> possibilitiesOfCompletion) {
		String argumentToFindCompletionFor = args[args.length - 1];

		List<String> listOfPossibleCompletions = new ArrayList<String>();

		for (String foundString : possibilitiesOfCompletion) {
			if (foundString.regionMatches(true, 0, argumentToFindCompletionFor, 0, argumentToFindCompletionFor.length())) {
				listOfPossibleCompletions.add(foundString);
			}
		}
		return listOfPossibleCompletions;
	}

	public static List<String> getPossibleCompletionsForGivenArgs(String[] args, String[] possibilitiesOfCompletion) {
		return getPossibleCompletionsForGivenArgs(args, Arrays.asList(possibilitiesOfCompletion));
	}

	/** Returns a list of subcommands the sender can use. */
	public static List<String> getCommandsForUser(CommandSender sender) {
		List<String> list = new ArrayList<String>();
		for (String cmd : PKCommand.instances.keySet()) {
			if (sender.hasPermission("bending.command." + cmd.toLowerCase()))
				list.add(cmd);
		}
		Collections.sort(list);
		return list;
	}

}
