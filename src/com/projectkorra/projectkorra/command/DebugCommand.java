package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending debug. Extends {@link PKCommand}.
 */
public class DebugCommand extends PKCommand {

	public DebugCommand() {
		super("debug", "/bending debug", ConfigManager.languageConfig.get().getString("Commands.Debug.Description"), new String[] { "debug", "de" });
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
		sender.sendMessage(ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Debug.SuccessfullyExported"));
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
			sender.sendMessage(super.noPermissionMessage);
			return false;
		}
		return true;
	}
}
