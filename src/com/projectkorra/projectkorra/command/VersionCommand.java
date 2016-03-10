package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending version. Extends {@link PKCommand}.
 */
public class VersionCommand extends PKCommand {

	public VersionCommand() {
		super("version", "/bending version", ConfigManager.languageConfig.get().getString("Commands.Version.Description"), new String[] { "version", "v" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Core Version: " + ChatColor.RED + ProjectKorra.plugin.getDescription().getVersion());
		if (GeneralMethods.hasRPG()) {
			sender.sendMessage(ChatColor.GREEN + "RPG Version: " + ChatColor.RED + GeneralMethods.getRPG().getDescription().getVersion());
		}
		if (GeneralMethods.hasItems()) {
			sender.sendMessage(ChatColor.GREEN + "Items Version: " + ChatColor.RED + GeneralMethods.getItems().getDescription().getVersion());
		}
		sender.sendMessage(ChatColor.GREEN + "Founded by: " + ChatColor.RED + "MistPhizzle");
		sender.sendMessage(ChatColor.GREEN + "Learn More: " + ChatColor.RED + "http://projectkorra.com");
	}

}
