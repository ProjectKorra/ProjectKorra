package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending reload. Extends {@link PKCommand}.
 */
public class ReloadCommand extends PKCommand {

	public ReloadCommand() {
		super("reload", "/bending reload", ConfigManager.languageConfig.get().getString("Commands.Reload.Description"), new String[] { "reload", "r" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		}
		GeneralMethods.reloadPlugin(sender);
		sender.sendMessage(ChatColor.AQUA + ConfigManager.languageConfig.get().getString("Commands.Reload.SuccessfullyReloaded"));
	}

}
