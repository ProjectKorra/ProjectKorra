package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.configs.commands.ReloadCommandConfig;

/**
 * Executor for /bending reload. Extends {@link PKCommand}.
 */
public class ReloadCommand extends PKCommand<ReloadCommandConfig> {

	public ReloadCommand(final ReloadCommandConfig config) {
		super(config, "reload", "/bending reload", config.Description, new String[] { "reload", "r" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}
		GeneralMethods.reloadPlugin(sender);
		sender.sendMessage(ChatColor.AQUA + config.SuccessfullyReloaded);
	}

}
