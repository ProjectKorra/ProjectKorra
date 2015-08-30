package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending check. Extends {@link PKCommand}.
 */
public class CheckCommand extends PKCommand {

	public CheckCommand() {
		super("check", "/bending check", "Checks if ProjectKorra is up to date.", new String[] { "check", "chk" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender)) {
			return;
		} else if (args.size() > 0) {
			help(sender, false);
			return;
		}
		if (ProjectKorra.plugin.updater.updateAvailable()) {
			sender.sendMessage(ChatColor.GREEN + "There is a new version of " + ChatColor.GOLD + "ProjectKorra" + ChatColor.GREEN + " available!");
			sender.sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + ProjectKorra.plugin.updater.getCurrentVersion());
			sender.sendMessage(ChatColor.YELLOW + "Latest version: " + ChatColor.GOLD + ProjectKorra.plugin.updater.getUpdateVersion());
		} else {
			sender.sendMessage(ChatColor.YELLOW + "You have the latest version of " + ChatColor.GOLD + "ProjectKorra");
		}
	}

}
