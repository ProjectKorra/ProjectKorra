package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.configs.commands.DebugCommandConfig;

/**
 * Executor for /bending debug. Extends {@link PKCommand}.
 */
public class DebugCommand extends PKCommand<DebugCommandConfig> {

	public DebugCommand(final DebugCommandConfig config) {
		super(config, "debug", "/bending debug", config.Description, new String[] { "debug", "de" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender)) {
			return;
		} else if (args.size() != 0) {
			this.help(sender, false);
			return;
		}

		GeneralMethods.runDebug();
		sender.sendMessage(ChatColor.GREEN + config.SuccessfullyExported);
	}

	/**
	 * Checks if the CommandSender has the permission 'bending.admin.debug'. If
	 * not, it tells them they don't have permission.
	 *
	 * @return True if they have permission, false otherwise.
	 */
	@Override
	public boolean hasPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin." + this.getName())) {
			sender.sendMessage(super.noPermissionMessage);
			return false;
		}
		return true;
	}
}
