package com.projectkorra.projectkorra.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Completes tabbing for the bending command/subcommands.
 * 
 * @author StrangeOne101
 * */
public class BendingTabComplete implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0 || args.length == 1)
			return getPossibleCompletions(args, getCommandsForUser(sender));
		else if (args.length > 1){
			for (PKCommand cmd : PKCommand.instances.values()) {
				if (Arrays.asList(cmd.getAliases()).contains(args[0].toLowerCase()) && sender.hasPermission("bending.command." + cmd.getName())) {
					List<String> newargs = new ArrayList<String>();
					for (int i = 1; i < args.length - 1; i++) {
						if (!(args[i].equals("") || args[i].equals(" ")) && args.length >= 1)
						newargs.add(args[i]);
					}
					return getPossibleCompletions(args, cmd.getTabCompletion(sender, newargs));
				}
			}
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
	public static List<String> getPossibleCompletions(String[] args, List<String> possibilitiesOfCompletion) {
		String argumentToFindCompletionFor = args[args.length - 1];

		List<String> listOfPossibleCompletions = new ArrayList<String>();

		for (String foundString : possibilitiesOfCompletion) {
			if (foundString.regionMatches(true, 0, argumentToFindCompletionFor, 0, argumentToFindCompletionFor.length())) {
				listOfPossibleCompletions.add(foundString);
			}
		}
		return listOfPossibleCompletions;
	}

	public static List<String> getPossibleCompletions(String[] args, String[] possibilitiesOfCompletion) {
		return getPossibleCompletions(args, Arrays.asList(possibilitiesOfCompletion));
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
