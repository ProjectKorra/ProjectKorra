package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending debug. Extends {@link PKCommand}.
 */
public class DebugCommand extends PKCommand {

	public DebugCommand() {
		super("debug", "/bending debug", "Outputs information on the current ProjectKorra installation to /plugins/ProjectKorra/debug.txt", new String[] { "debug", "de" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender)) {
			return;
		} else if (args.size() != 0) {
			help(sender, false);
			return;
		}

		GeneralMethods.runDebug();
		sender.sendMessage(ChatColor.GREEN + "Debug File Created as debug.txt in the ProjectKorra plugin folder.");
		sender.sendMessage(ChatColor.GREEN + "Put contents on pastie.org and create a bug report  on the ProjectKorra forum if you need to.");
	}

	/**
	 * Checks if the CommandSender has the permission 'bending.admin.debug'. If
	 * not, it tells them they don't have permission.
	 * 
	 * @return True if they have permission, false otherwise.
	 */
	@Override
	public boolean hasPermission(CommandSender sender) {
		if (!sender.hasPermission("bending.admin." + getName())) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			return false;
		}
		return true;
	}
}
