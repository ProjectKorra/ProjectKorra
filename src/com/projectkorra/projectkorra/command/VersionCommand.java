package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending version. Extends {@link PKCommand}.
 */
public class VersionCommand extends PKCommand {

	public VersionCommand() {
		super("version", "/bending version", ConfigManager.languageConfig.get().getString("Commands.Version.Description"), new String[] { "version", "v" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Core Version: " + ChatColor.RED + ProjectKorra.plugin.getDescription().getVersion() + " -BMC-Changes");
		if (GeneralMethods.hasRPG()) {
			sender.sendMessage(ChatColor.GREEN + "RPG Version: " + ChatColor.RED + GeneralMethods.getRPG().getDescription().getVersion());
		}
		if (GeneralMethods.hasItems()) {
			sender.sendMessage(ChatColor.GREEN + "Items Version: " + ChatColor.RED + GeneralMethods.getItems().getDescription().getVersion());
		}
		if (GeneralMethods.hasSpirits()) {
			sender.sendMessage(ChatColor.GREEN + "Spirits Version: " + ChatColor.RED + GeneralMethods.getSpirits().getDescription().getVersion());
		}
		if (GeneralMethods.hasProbending()) {
			sender.sendMessage(ChatColor.GREEN + "Probending Version: " + ChatColor.RED + GeneralMethods.getProbending().getDescription().getVersion());
		}
		sender.sendMessage(ChatColor.GREEN + "Founded by: " + ChatColor.RED + "MistPhizzle");
		sender.sendMessage(ChatColor.GREEN + "Special thanks to " + ChatColor.RED + "Orion304 " + ChatColor.GREEN + "for establishing this great community.");
		sender.sendMessage(ChatColor.GREEN + "Learn More: " + ChatColor.RED + "http://projectkorra.com");
	}

}
