package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Completes tabbing for the bending command/subcommands.
 *
 * @author StrangeOne101
 */
public class BendingTabComplete implements TabCompleter {

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
		if (args.length == 0 || args.length == 1) {
			return getPossibleCompletions(args, getCommandsForUser(sender));
		}

		for (final PKCommand cmd : PKCommand.instances.values()) {
			if (Arrays.asList(cmd.getAliases()).contains(args[0].toLowerCase()) && cmd.hasPermission(sender)) {
				final List<String> subArgs = new ArrayList<>();
				for (int i = 1; i < args.length - 1; i++) {
					if (!(args[i].isEmpty() || args[i].equals(" "))) {
						subArgs.add(args[i]);
					}
				}
				return getPossibleCompletions(args, cmd.getTabCompletion(sender, subArgs));
			}
		}

		return List.of();
	}

	/**
	 * Breaks down the possible list and returns what is applicable depending on
	 * what the user has currently typed.
	 *
	 * @author D4rKDeagle<br><br>
	 *         (Found at <a href=https://bukkit.org/threads/help-with-bukkit-tab-completion-api.166436></a>)
	 * @param args Args of the command. Provide all of them.
	 * @param possibilitiesOfCompletion List of things that can be given
	 */
	public static List<String> getPossibleCompletions(final String[] args, final List<String> possibilitiesOfCompletion) {
		final String argumentToFindCompletionFor = args[args.length - 1];

		final List<String> listOfPossibleCompletions = new ArrayList<>();

		for (final String foundString : possibilitiesOfCompletion) {
			if (foundString.regionMatches(true, 0, argumentToFindCompletionFor, 0, argumentToFindCompletionFor.length())) {
				listOfPossibleCompletions.add(foundString);
			}
		}
		return listOfPossibleCompletions;
	}

	/** Returns a list of subcommands the sender can use. */
	public static List<String> getCommandsForUser(final CommandSender sender) {
		final List<String> list = new ArrayList<>();
		for (final PKCommand cmd : PKCommand.instances.values()) {
			if (cmd.hasPermission(sender)) {
				list.add(cmd.getName());
			}
		}
		Collections.sort(list);
		return list;
	}
}
